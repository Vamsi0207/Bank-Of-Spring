package com.banking.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    
    @Autowired
    private JavaMailSender mailSender;

    public void sendMail(String to,String subject,String text){
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom("bankofspring@gmail.com");  //From Mail account
        message.setTo(to);                                 //To Mail account
        message.setSubject(subject);                    //Mail Subject
        message.setText(text);                          //mail body

        mailSender.send(message);
    }
}
