package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner{

	@Autowired
	private Environment env;

	@Value("${server.port}")
	String port;

	public static void main(String[] args){
		SpringApplication.run(DemoApplication.class,args);
	}

	@Override
	public void run(String... args) throws Exception{

		System.out.println("Server,port:"+port);
		System.out.println("Bean:");

		String[] activeProfiles = env.getActiveProfiles();
		if(activeProfiles.length == 0){
			System.out.println("active.profiles = (none)");
		}else{
			System.out.println("active.profiles = " + String.join(",",activeProfiles));
		}
	}
}