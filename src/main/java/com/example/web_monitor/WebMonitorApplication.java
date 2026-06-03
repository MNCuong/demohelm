package com.example.web_monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WebMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebMonitorApplication.class, args);
	}

}
