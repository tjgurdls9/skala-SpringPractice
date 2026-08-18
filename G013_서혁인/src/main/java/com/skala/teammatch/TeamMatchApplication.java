package com.skala.teammatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TeamMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamMatchApplication.class, args);
    }
}
