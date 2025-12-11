package com.cognitive.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableCaching
@SpringBootApplication
public class CognitiveBankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CognitiveBankingApplication.class, args);
    }
}
