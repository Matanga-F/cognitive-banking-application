package com.cognitive.banking.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "file:.env", ignoreResourceNotFound = true)
public class EnvConfig {
    // This class intentionally left blank – just loads the .env file
}