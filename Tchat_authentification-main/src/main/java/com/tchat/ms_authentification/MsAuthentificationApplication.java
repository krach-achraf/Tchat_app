package com.tchat.ms_authentification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsAuthentificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAuthentificationApplication.class, args);
	}

}
