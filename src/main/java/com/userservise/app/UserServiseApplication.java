package com.userservise.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UserServiseApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiseApplication.class, args);
	}

}
