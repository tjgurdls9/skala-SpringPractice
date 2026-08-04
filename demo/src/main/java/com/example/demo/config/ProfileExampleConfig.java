package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ProfileExampleConfig{

    @Bean
    @Profile("dev")
    public String devBean(){
        return "dev Bean";
    }
    @Bean
    @Profile("Prod")
    public String prodString(){
        return "Prod Bean";
    }
}