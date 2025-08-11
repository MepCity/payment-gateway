package com.payment.gateway;

import com.payment.gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class GatewayApplication implements CommandLineRunner {

	private final UserService userService;

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Create default users on startup
		userService.createDefaultUsers();
	}
}
