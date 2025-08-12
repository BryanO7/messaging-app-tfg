package com.tfgproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 👈 ESTO ES CRÍTICO
public class MessagingAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessagingAppApplication.class, args);
    }
}