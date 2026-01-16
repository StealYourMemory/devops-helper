package com.coderzoe.controller;

import com.coderzoe.common.Result;
import com.coderzoe.common.ThreadPool;
import com.coderzoe.common.log.Log;
import com.coderzoe.common.util.UserGetter;
import com.coderzoe.component.seafile.model.FileTree;
import com.coderzoe.component.seafile.service.SeafileArchiveService;
import com.coderzoe.component.seafile.service.SeafileService;
import com.coderzoe.component.seafile.service.SeafileTransferService;
import com.coderzoe.model.request.BuildAndTransferRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author yinhuasheng
 * @date 2024/8/20 16:44
 */
@RestController
@RequestMapping("/seafile")
@Setter(onMethod = @__(@Autowired))
public class SeafileController {
    private SeafileService seafileService;
    private SeafileTransferService seafileTransferService;
    private SeafileArchiveService seafileArchiveService;

    @Log
    @GetMapping("/directory")
    public Result<List<FileTree>> getDirectory(@RequestParam String path) {
        return seafileService.getDirInfo(path);
    }

    @Log
    @GetMapping(value = "/file")
    public Result<String> getFile(@RequestParam String path) {
        return seafileService.getFileContent(path);
    }

    @Log
    @GetMapping(value = "/deep")
    public Result<List<FileTree>> getDeep(@RequestParam String path) {
        return Result.success(seafileService.deepGetDirInfo(path));
    }

    @Log
    @PostMapping("/transfer")
    public Result<String> transfer(@RequestBody BuildAndTransferRequest request, HttpServletRequest servletRequest) {
        String userName = UserGetter.get(servletRequest);
        String requestId = UUID.randomUUID().toString();
        ThreadPool.executorService().submit(() -> seafileTransferService.buildAndTransfer(request, userName, requestId));
        return Result.success(requestId);
    }

    @Log
    @PostMapping("/archive")
    public Result<Void> archive(@RequestParam String path) {
        seafileArchiveService.archive(path);
        return Result.success(null);
    }

    @Log
    @GetMapping("/root/directory")
    public Result<String> rootDirectory() {
        return Result.success(seafileService.rootDir());
    }
}
