package com.ls.spaceBookingSystem;

import com.ls.spaceBookingSystem.services.TokenBlacklistService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.data.redis.RedisHealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpaceBookingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpaceBookingSystemApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public CommandLineRunner run(TokenBlacklistService tokenBlacklistService) {
		return args -> tokenBlacklistService.test();
	}

}
