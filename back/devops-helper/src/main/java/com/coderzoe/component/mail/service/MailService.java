package com.coderzoe.component.mail.service;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:54
 */
@Service
@Setter(onMethod_ = {@Autowired})
public class MailService {
    private JavaMailSender javaMailSender;

    public void sendMail(String fromEmail, String toEmail, String subject, String text) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromEmail);
        simpleMailMessage.setTo(toEmail);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(text);
        javaMailSender.send(simpleMailMessage);
    }
}
