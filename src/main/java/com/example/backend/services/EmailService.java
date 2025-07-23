package com.example.backend.services;

import com.example.backend.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Value("${frontend-link}")
    private String frontLink;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetLinkToUser(User user, String tokenValue) {
        String link = frontLink+"/auth/reset?token=" + tokenValue;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Reset Request");
        message.setText("Click the following link to reset your password:\n" + link);
        mailSender.send(message);
    }
}
