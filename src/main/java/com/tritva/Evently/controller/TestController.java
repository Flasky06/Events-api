package com.tritva.Evently.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")

public class TestController {

    private final JavaMailSender mailSender;


    @GetMapping("/test-mail")
    public String sendMail() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("bonnienjuguna106@gmail.com");
        msg.setSubject("Test from Spring Boot");
        msg.setText("Hello! This is a test email.");

        mailSender.send(msg);
        return "Mail sent!";
    }
}
