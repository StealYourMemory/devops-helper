package com.coderzoe.controller;

import com.coderzoe.common.Result;
import com.coderzoe.common.log.Log;
import com.coderzoe.component.k8s.model.K8sEnvAndServerInfo;
import com.coderzoe.component.k8s.model.database.K8sProxyRecordDO;
import com.coderzoe.component.k8s.model.request.AddServerRequest;
import com.coderzoe.component.k8s.model.request.DeleteServerRequest;
import com.coderzoe.component.k8s.model.request.ProxyRecordQueryRequest;
import com.coderzoe.component.k8s.model.request.ProxyRequest;
import com.coderzoe.component.k8s.service.K8sProxyService;
import com.coderzoe.component.k8s.service.K8sServerService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author yinhuasheng
 * @date 2024/8/19 19:25
 */
@RestController
@RequestMapping("/k8s")
@Setter(onMethod_ = {@Autowired})
public class K8sController {
    private K8sServerService k8sServerService;
    private K8sProxyService k8sProxyService;

    @Log
    @PostMapping("/env")
    public Result<Void> createEnvironment(@RequestBody K8sEnvAndServerInfo envAndServerInfo) throws Exception {
        k8sServerService.createEnvAndServer(envAndServerInfo);

        return Result.success(null);
    }

    @Log
    @PostMapping("/server")
    public Result<Void> addServer(@RequestBody AddServerRequest request) throws Exception {
        k8sServerService.addServer(request);
        return Result.success(null);
    }

    @Log
    @DeleteMapping("/env/{name}")
    public Result<Void> deleteEnvironment(@PathVariable String name) {
        k8sServerService.deleteEnv(name);
        return Result.success(null);
    }

    @Log
    @DeleteMapping("/server")
    public Result<Void> deleteServer(@RequestBody DeleteServerRequest request) {
        k8sServerService.deleteServer(request);
        return Result.success(null);
    }

    @Log
    @PostMapping("/proxy/query")
    public Result<ProxyRecordQueryRequest> queryProxy(@RequestBody ProxyRecordQueryRequest request) {
        return k8sProxyService.proxyRecordQuery(request);
    }

    @Log
    @PostMapping("/proxy")
    public Result<K8sProxyRecordDO> proxy(@RequestBody ProxyRequest request) throws IOException {
        return k8sProxyService.proxy(request);
    }

    @Log
    @DeleteMapping("/proxy/{id}")
    public Result<K8sProxyRecordDO> deleteProxy(@PathVariable("id") long id) {
        return k8sProxyService.deleteProxy(id);
    }
}
