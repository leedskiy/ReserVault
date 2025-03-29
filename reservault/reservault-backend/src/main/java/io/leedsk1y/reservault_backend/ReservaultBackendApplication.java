package io.leedsk1y.reservault_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ReservaultBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReservaultBackendApplication.class, args);
	}
}
