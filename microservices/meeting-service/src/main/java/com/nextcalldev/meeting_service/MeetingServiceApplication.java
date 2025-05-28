package com.nextcalldev.meeting_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MeetingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetingServiceApplication.class, args);
	}

}
