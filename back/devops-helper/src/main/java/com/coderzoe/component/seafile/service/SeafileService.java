package com.coderzoe.component.seafile.service;

import com.coderzoe.common.Result;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.component.seafile.config.SeafileConfig;
import com.coderzoe.component.seafile.model.FileTree;
import com.coderzoe.component.seafile.model.request.DeleteDirRequest;
import com.coderzoe.component.seafile.model.response.DeleteDirResponse;
import com.coderzoe.component.seafile.model.response.DirectoryResponse;
import com.coderzoe.component.seafile.model.response.UploadResponse;
import com.coderzoe.model.pojo.LoginCookie;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * seafile常用api
 * 如查询目录树
 * 增删改查文件
 * 查询文件内容等
 *
 * @author yinhuasheng
 * @date 2024/8/20 10:55
 */
@Slf4j
@Service
@Setter(onMethod = @__(@Autowired))
public class SeafileService {
    private SeafileLoginService seafileLoginService;
    private SeafileConfig seafileConfig;
    private RestTemplate restTemplate;

    private static final String FILE_NOT_EXIST_SYMBOL = "<p class=\"error\">文件不存在</p>";


    /**
     * 读取文本文件内容
     *
     * @param filePath 文件路径
     * @return 返回文件内容（文本文件）
     */
    public Result<String> getFileContent(String filePath) {
        Result<String> result = doGetFileContent(filePath);
        if (!result.isSuccess()) {
            Result<LoginCookie> login = seafileLoginService.login();
            if (!login.isSuccess()) {
                throw new CommonException("seafile登录失败，失败原因:" + login.getMsg());
            }
            result = doGetFileContent(filePath);
        }
        return result;
    }

    /**
     * 获得某个目录下的信息，不递归获取，只获取一层
     *
     * @param directory 目录路径
     * @return 返回目录树
     */
    public Result<List<FileTree>> getDirInfo(String directory) {
        Result<List<FileTree>> result = doGetDirInfo(directory);
        if (!result.isSuccess()) {
            Result<LoginCookie> login = seafileLoginService.login();
            if (!login.isSuccess()) {
                throw new CommonException("seafile登录失败，失败原因:" + login.getMsg());
            }
            result = doGetDirInfo(directory);
        }
        return result;
    }

    /**
     * 深度遍历获取某路径下文件树，递归获取，获取所有子孙目录的内容
     *
     * @param directory 父目录
     * @return 返回目录树
     */
    public List<FileTree> deepGetDirInfo(String directory) {
        Result<List<FileTree>> result = getDirInfo(directory);
        if (result.isSuccess() && !CollectionUtils.isEmpty(result.getData())) {
            for (FileTree fileTree : result.getData()) {
                //如果子元素是目录，递归获取
                if (!fileTree.isLeaf()) {
                    String childPath = directory + "/" + fileTree.getName();
                    fileTree.setChildren(deepGetDirInfo(childPath));
                }
            }
            return result.getData();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 创建目录
     *
     * @param path 目录路径
     * @return 返回是否创建成功
     */
    public Result<String> createDir(String path) {
        URI uri = getDirectoryUri(path);
        HttpHeaders headers = getSeafileHttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("operation", "mkdir");
        ResponseEntity<String> response = restTemplate.exchange(uri,
                HttpMethod.POST,
                new HttpEntity<>(formData, headers),
                String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return Result.success(response.getBody());
        } else {
            return Result.fail("创建目录" + path + "失败", "失败响应码", response.getStatusCode().toString(), "失败详情", response.getBody());
        }
    }

    /**
     * 删除目录
     *
     * @param parentDir     父目录
     * @param deleteDirList 要删除的父目录下的子目录
     * @return 返回是否删除成功
     */
    public Boolean deleteDir(String parentDir, List<String> deleteDirList) {
        DeleteDirRequest request = new DeleteDirRequest(parentDir, deleteDirList, seafileConfig.getRepositoryId());
        HttpHeaders headers = getSeafileHttpHeaders();
        ResponseEntity<DeleteDirResponse> resultResponseEntity = restTemplate.exchange(seafileConfig.getDeleteDirUrl(),
                HttpMethod.DELETE,
                new HttpEntity<>(request, headers),
                DeleteDirResponse.class);
        if (resultResponseEntity.getStatusCode().is2xxSuccessful()) {
            return Objects.requireNonNull(resultResponseEntity.getBody()).isSuccess();
        } else {
            log.error("删除父目录:{}下的子目录:{}失败，失败响应码：{}，失败详情:{}", parentDir, deleteDirList, resultResponseEntity.getStatusCode(), resultResponseEntity.getBody());
            return false;
        }
    }

    /**
     * 上传文件
     *
     * @param path     文件路径
     * @param file     文件内容
     * @param fileName 文件名
     * @return 返回是否上传成功
     */
    public Result<List<UploadResponse>> uploadFile(String path, byte[] file, String fileName) {
        Result<String> urlResult = getUploadFileUrl(path);
        if (urlResult.isSuccess()) {
            return doUploadFile(fileName, path, file, urlResult.getData());
        } else {
            return Result.fail(urlResult.getMsg());
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 返回是否删除成功
     */
    public Result<String> deleteFile(String filePath) {
        URI uri = getFileUri(filePath);
        HttpHeaders headers = getSeafileHttpHeaders();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return Result.success(response.getBody());
        } else {
            return Result.fail("删除文件失败，响应码" + response.getStatusCode(), "响应内容" + response.getBody());
        }
    }

    public String rootDir(){
        return seafileConfig.getBasePath();
    }

    private Result<String> doGetFileContent(String filePath) {
        try {
            HttpHeaders headers = getSeafileHttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            /*
             * 注：这里有个坑，文件请求的时候响应头写的是gbk，但实际文件却是utf-8编码，这会导致乱码
             * 如果restTemplate的请求结果泛型写为String，那会按响应头给的gbk编码，最终会乱码
             * 如果按gbk编码后再按gbk解码为字节数组，再编码为utf-8依然会乱码
             * 原因 详见 https://cloud.tencent.com/developer/article/1532276
             * 与 https://blog.csdn.net/csdn_ds/article/details/79077483
             */
            ResponseEntity<byte[]> response = restTemplate.exchange(getDownloadFileUri(filePath), HttpMethod.GET, entity, byte[].class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String content = new String(Objects.requireNonNull(response.getBody()), StandardCharsets.UTF_8);
                if (content.contains(FILE_NOT_EXIST_SYMBOL)) {
                    return Result.fail("文件不存在");
                }
                return Result.success(content);
            } else {
                return Result.fail("HTTP响应码:", response.getStatusCode().toString(), "可能是由于seafile登录失效");
            }
        } catch (Exception e) {
            return Result.fail("请求失败可能是由于seafile登录失效");
        }
    }


    private Result<List<FileTree>> doGetDirInfo(String directory) {
        try {
            HttpHeaders headers = getSeafileHttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<DirectoryResponse> response = restTemplate.exchange(getDirectoryUri(directory), HttpMethod.GET, entity, DirectoryResponse.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Objects.requireNonNull(response.getBody());
                List<FileTree> fileTreeList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(response.getBody().getDirectoryInfoList())) {
                    fileTreeList = response.getBody().getDirectoryInfoList().stream().map(FileTree::new).collect(Collectors.toList());
                }
                return Result.success(fileTreeList);
            } else {
                return Result.fail("HTTP响应码:", response.getStatusCode().toString(), "可能是由于seafile登录失效");
            }
        } catch (Exception e) {
            return Result.fail("请求失败可能是由于seafile登录失效");
        }
    }

    /**
     * 上传文件是先执行一个get 拿到上传地址再上传
     *
     * @param path 路径
     * @return 返回上传地址
     */
    private Result<String> getUploadFileUrl(String path) {
        HttpHeaders headers = this.getSeafileHttpHeaders();
        ResponseEntity<String> response = restTemplate.exchange(getUploadFileUri(path), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            String url = Objects.requireNonNull(response.getBody()).replace("\"", "") + "?ret-json=1";
            return Result.success(url);
        } else {
            return Result.fail("获取上传路径失败，HTTP响应码:", response.getStatusCode().toString(), "响应结果:" + response.getBody());
        }
    }

    /**
     * 文件上传，这里解释几点， 源seafile前端是使用的Resumable.js做的上传，这其中有两点：
     * 1. 分片上传，项目做了分片上传的机制，一次分片8kb,但我图省事没做分片
     * 因为第一我们的文件不会很大，第二分片上传只在断点续传时好使，但加上断点续传太麻烦
     * 2. Resumable.js有个resumableIdentifier用来做可靠性校验的
     * 一般情况可能是文件字节+一些其他信息形成的md5，然后服务端使用相同算法校验md5是否相同，避免请求被篡改
     * 但我扒了seafile的前端代码，发现它的md5生成算法是
     * <code>
     * generateUniqueIdentifier = (file) => {
     * let relativePath = file.webkitRelativePath||file.relativePath||file.fileName||file.name;
     * return MD5(relativePath + new Date()) + relativePath;
     * }
     * </code>
     * 由于带的是new Date()，前后端时间不可能完全一致，因此可以判定这个md5没有意义，后端不会校验
     * 但即使如此，笔者还是尽可能的遵从了源代码的实现，改为用Java Date
     * 不过需要明确的是，js的new Date()对应的字符串和笔者这里java写的Date对应的字符串是完全不同的
     * 对于resumableIdentifier笔者并没有还原源算法，只是也借此思想实现了类似的生成方法，因为服务端不校验，所以应该不填也没事
     *
     * @param fileName 文件名
     * @param path     上传路径
     * @param file     文件字节
     * @param url      上传的url
     * @return 返回是否上传成功
     */
    private Result<List<UploadResponse>> doUploadFile(String fileName, String path, byte[] file, String url) {
        HttpHeaders headers = this.getSeafileHttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("resumableChunkNumber", 1);
        body.add("resumableChunkSize", file.length);
        body.add("resumableCurrentChunkSize", file.length);
        body.add("resumableTotalSize", file.length);
        body.add("resumableType", "");
        body.add("resumableIdentifier", fileIdentityMd5(fileName));
        body.add("resumableFilename", fileName);
        body.add("resumableRelativePath", fileName);
        body.add("resumableTotalChunks", 1);
        body.add("parent_dir", path);
        body.add("file", new ByteArrayResource(file) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });
        ResponseEntity<List<UploadResponse>> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), new ParameterizedTypeReference<>() {
        });
        if (response.getStatusCode().is2xxSuccessful()) {
            return Result.success(response.getBody());
        } else {
            return Result.fail("上传失败，失败码" + response.getStatusCode() + "，失败原因:" + response.getBody());
        }
    }


    private URI getUploadFileUri(String filePath) {
        try {
            String encodedUrl = URLEncoder.encode(filePath, StandardCharsets.UTF_8);
            return new URI(seafileConfig.getBaseUploadUrl() + encodedUrl + seafileConfig.getBaseUploadSuffix());
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public HttpHeaders getSeafileHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        LoginCookie loginCookie = seafileLoginService.getCookie();
        headers.add("Cookie", loginCookie.toString());
        headers.add("X-Csrftoken", loginCookie.getCookies().get(1).split("=")[1]);
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        return headers;
    }

    private URI getDirectoryUri(String directory) {
        try {
            String encodedUrl = URLEncoder.encode(directory, StandardCharsets.UTF_8);
            return new URI(seafileConfig.getBaseDirectoryUrl() + "?p=" + encodedUrl + seafileConfig.getSuffix());
        } catch (Exception e) {
            log.error("SeafileService#getDirectoryUri 异常", e);
            throw new CommonException(e);
        }
    }

    private URI getFileUri(String filePath) {
        try {
            String encodedUrl = URLEncoder.encode(filePath, StandardCharsets.UTF_8);
            return new URI(seafileConfig.getBaseFileUrl() + "?p=" + encodedUrl);
        } catch (Exception e) {
            log.error("SeafileService#getFileUri 异常", e);
            throw new CommonException(e);
        }
    }

    private URI getDownloadFileUri(String filePath) {
        try {
            String[] split = filePath.split("/");
            StringBuilder builder = new StringBuilder();
            for (String path : split) {
                if (StringUtils.hasLength(path)) {
                    builder.append("/").append(new URI(null, null, path, null));
                }
            }
            return new URI(seafileConfig.getBaseDownloadFileUrl() + builder + "?dl=1");
        } catch (Exception e) {
            log.error("SeafileService#getDownloadFileUri 异常", e);
            throw new CommonException(e);
        }
    }

    private static String fileIdentityMd5(String fileName) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String formattedDate = sdf.format(new Date());
            String str = fileName + formattedDate;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(str.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb + fileName;
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }
}
