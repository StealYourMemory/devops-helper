package com.coderzoe.component.k8s.service;

import com.coderzoe.common.Result;
import com.coderzoe.component.k8s.config.K8sConfig;
import com.coderzoe.component.k8s.device.Device;
import com.coderzoe.component.k8s.device.DeviceManage;
import com.coderzoe.component.k8s.model.database.K8sProxyRecordDO;
import com.coderzoe.component.k8s.model.request.CmdRequest;
import com.coderzoe.component.k8s.model.request.ProxyRecordQueryRequest;
import com.coderzoe.component.k8s.model.request.ProxyRequest;
import com.coderzoe.component.k8s.model.response.CmdResponse;
import com.coderzoe.component.k8s.repository.K8sProxyRecordRepository;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * k8s代理service
 * 借助k8s自带的service做代理，其实就是linux的ipvs
 *
 * @author yinhuasheng
 * @date 2024/8/19 17:11
 */
@Service
@Setter(onMethod = @__(@Autowired))
public class K8sProxyService {
    private ResourceLoader resourceLoader;
    private K8sConfig k8sConfig;
    private K8sProxyRecordRepository k8sProxyRecordRepository;

    /**
     * 逻辑如下：
     * 先生成代理的endpoint和service内容
     * 再连接代理服务器，通过echo 将内容写入代理服务器
     * 再给代理服务器发指令 kubectl apply -f 启动代理
     *
     * @param request 代理请求信息
     * @return 返回各个处理的响应
     * @throws IOException 可能抛异常
     */
    public Result<K8sProxyRecordDO> proxy(ProxyRequest request) throws IOException {

        if (!StringUtils.hasLength(request.getProxyHost())) {
            Device device = DeviceManage.get(request.getEnvName()).getAnyOrThrow();
            request.setProxyHost(device.metaData().getIp());
        }
        Device proxyDevice = DeviceManage.get(request.getEnvName()).getByIpOrThrow(request.getProxyHost());

        //先建目录
        Result<CmdResponse> result = proxyDevice.send(new CmdRequest(k8sConfig.getMkDirSaveProxyPath()));
        if (!result.isSuccess()) {
            return Result.fail(result.getMsg());
        }

        //构造文件路径和文件内容
        var tag = "p" + UUID.randomUUID().toString().replace("-", "");

        String endPointFilePath = getEndPointFilePath(request.getSrcHost(), request.getSrcPort());
        String serviceFilePath = getServiceFilePath(request.getSrcHost(), request.getSrcPort(), request.getProxyPort());

        String endPointContent = String.format(readEndPoint(), tag, request.getSrcHost(), request.getSrcPort());
        String serviceContent = String.format(readService(), tag, request.getSrcPort(), request.getSrcPort(), request.getProxyPort());

        //echo 写入endPoint和service
        String cmd = "echo " + "\"" + endPointContent + "\"" + " > " + endPointFilePath;
        result = proxyDevice.send(new CmdRequest(cmd));
        if (!result.isSuccess()) {
            return Result.fail(result.getMsg());
        }

        cmd = "echo " + "\"" + serviceContent + "\"" + " > " + serviceFilePath;
        result = proxyDevice.send(new CmdRequest(cmd));
        if (!result.isSuccess()) {
            return Result.fail(result.getMsg());
        }

        //apply -f内容
        String applyCmd = "kubectl apply -f " + endPointFilePath;
        result = proxyDevice.send(new CmdRequest(applyCmd));
        if (!result.isSuccess()) {
            return Result.fail(result.getMsg());
        }

        applyCmd = "kubectl apply -f " + serviceFilePath;
        result = proxyDevice.send(new CmdRequest(applyCmd));
        if (!result.isSuccess()) {
            return Result.fail(result.getMsg());
        }

        K8sProxyRecordDO record = new K8sProxyRecordDO(request.getEnvName(), request.getProxyHost(), request.getProxyPort(), request.getSrcHost(), request.getSrcPort(), request.getDescription());
        k8sProxyRecordRepository.save(record);
        return Result.success(record);
    }


    public Result<K8sProxyRecordDO> deleteProxy(long id) {
        K8sProxyRecordDO proxy = k8sProxyRecordRepository.findById(id).orElseThrow(() -> new RuntimeException("未找到代理记录"));

        Device proxyDevice = DeviceManage.get(proxy.getEnvName()).getByIpOrThrow(proxy.getProxyHost());

        String serviceFilePath = getServiceFilePath(proxy.getSrcHost(), proxy.getSrcPort(), proxy.getProxyPort());
        String endPointFilePath = getEndPointFilePath(proxy.getSrcHost(), proxy.getSrcPort());

        //删除代理
        String cmd = "kubectl delete -f ";

        proxyDevice.send(new CmdRequest(cmd + serviceFilePath));
        proxyDevice.send(new CmdRequest(cmd + endPointFilePath));

        //删除文件
        cmd = "rm -f ";
        proxyDevice.send(new CmdRequest(cmd + serviceFilePath));
        proxyDevice.send(new CmdRequest(cmd + endPointFilePath));

        //删除库记录
        k8sProxyRecordRepository.deleteById(id);
        return Result.success(proxy);
    }


    public Result<ProxyRecordQueryRequest> proxyRecordQuery(ProxyRecordQueryRequest request) {
        Pageable pageable;
        Sort sort = Sort.by("id").descending();
        if (request.getPaging()) {
            pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);
        } else {
            pageable = Pageable.unpaged(sort);
        }
        Specification<K8sProxyRecordDO> spec = Specification.where(null);
        if (StringUtils.hasLength(request.getEnvName())) {
            spec = spec.and(Specifications.envNameStartsWith(request.getEnvName()));
        }
        if (StringUtils.hasLength(request.getProxyHost())) {
            spec = spec.and(Specifications.proxyHostStartsWith(request.getProxyHost()));
        }
        if (StringUtils.hasLength(request.getSrcHost())) {
            spec = spec.and(Specifications.srcHostStartsWith(request.getSrcHost()));
        }
        if (request.getStartTime() != null || request.getEndTime() != null) {
            spec = spec.and(Specifications.timeBetweenAnd(request.getStartTime(), request.getEndTime()));
        }
        Page<K8sProxyRecordDO> page = k8sProxyRecordRepository.findAll(spec, pageable);
        request.setTotal(page.getTotalElements());
        request.setRecords(page.toList());
        return Result.success(request);
    }


    private String readEndPoint() throws IOException {
        return readYamlFile("proxy/endpoint.yaml");
    }

    private String readService() throws IOException {
        return readYamlFile("proxy/service.yaml");
    }

    public String readYamlFile(String filePath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + filePath);
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private String getEndPointFilePath(String srcHost, int srcPort) {
        String endPointFileName = "endpoint_" + srcHost.replace(".", "_") + "_" + srcPort + ".yaml";
        return k8sConfig.getSaveProxyPath() + endPointFileName;
    }

    private String getServiceFilePath(String srcHost, int srcPort, int proxyPort) {
        String serviceFileName = "service_" + srcHost.replace(".", "_") + "_" + srcPort + "_" + proxyPort + ".yaml";
        return k8sConfig.getSaveProxyPath() + serviceFileName;
    }

    static class Specifications {
        public static Specification<K8sProxyRecordDO> envNameStartsWith(String envName) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("envName"), envName + "%");
        }

        public static Specification<K8sProxyRecordDO> proxyHostStartsWith(String proxyHost) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("proxyHost"), proxyHost + "%");
        }

        public static Specification<K8sProxyRecordDO> srcHostStartsWith(String srcHost) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("srcHost"), srcHost + "%");
        }

        public static Specification<K8sProxyRecordDO> timeBetweenAnd(Long startTime, Long endTime) {
            return (root, query, criteriaBuilder) -> {
                Date effectiveStartTime = startTime != null ? new Date(startTime) : new Date(0);
                Date effectiveEndTime = endTime != null ? new Date(endTime) : new Date();
                return criteriaBuilder.between(root.get("createTime"), effectiveStartTime, effectiveEndTime);
            };
        }
    }
}
