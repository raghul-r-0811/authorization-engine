package org.raghul.authorizationengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthorizationEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthorizationEngineApplication.class, args);
		System.out.println("Initial Set up done");
	}

}
