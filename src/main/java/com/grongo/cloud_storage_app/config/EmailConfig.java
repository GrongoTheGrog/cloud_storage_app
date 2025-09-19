package com.grongo.cloud_storage_app.config;


import com.grongo.cloud_storage_app.exceptions.email.EmailConfigurationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${EMAIL_USERNAME}")
    String emailUsername;

    @Value("${EMAIL_PASSWORD}")
    String emailPassword;

    @Bean
    public JavaMailSender javaMailSender(){
        if (emailPassword == null || emailUsername == null){
            throw new EmailConfigurationException("Email configuration must be set as env variables.");
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername(emailUsername);
        mailSender.setPassword(emailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
