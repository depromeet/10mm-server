package com.depromeet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TenminuteApplication {
    public static void main(String[] args) {
        SpringApplication.run(TenminuteApplication.class, args);
    }
}
