package com.coderzoe.component.k8s.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/8/19 16:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SftpRequest {
    private String srcFilePath;
    private String dstFilePath;
}
