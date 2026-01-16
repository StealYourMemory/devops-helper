package com.coderzoe.component.k8s.model.request;

import com.coderzoe.component.k8s.device.Device;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/8/21 16:03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScpRequest {
    private String srcFilePath;
    private String dstFilePath;
    private Device srcDevice;
    private Device dstDevice;
}
