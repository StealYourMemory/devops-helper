package com.coderzoe.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:58
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailSendRequest {
    private String userName;
    private String sendContent;
    private String subject;
}
