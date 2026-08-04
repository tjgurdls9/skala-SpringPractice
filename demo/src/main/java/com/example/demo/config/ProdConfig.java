package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("Prod")
public class ProdConfig {

    public ProdConfig(){
        System.out.println("ProdConfig");
    }
}