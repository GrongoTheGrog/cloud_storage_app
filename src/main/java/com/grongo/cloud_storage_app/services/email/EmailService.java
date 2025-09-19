package com.grongo.cloud_storage_app.services.email;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${EMAIL_USERNAME}")
    String email;

    public MessageBuilder message(){
        return new MessageBuilder();
    }

    @Async
    public void send(SimpleMailMessage message){
        message.setFrom(email);
        javaMailSender.send(message);
    }

    public class MessageBuilder{
        public MessageBuilder(){};

        private final SimpleMailMessage message = new SimpleMailMessage();

        public MessageBuilder setTo(String toEmail){
            message.setTo(toEmail);
            return this;
        }

        public MessageBuilder setText(String text){
            message.setText(text);
            return this;
        }

        public MessageBuilder setSubject(String subject){
            message.setSubject(subject);
            return this;
        }

        public SimpleMailMessage build(){
            return message;
        }
    }

}
