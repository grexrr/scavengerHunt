package com.scavengerhunt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScavengerhuntApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScavengerhuntApplication.class, args);
	}

}
