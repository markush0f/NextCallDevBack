package com.nextcalldev.meeting_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nextcalldev.meeting_service.models.dto.UserResponseDto;

@FeignClient(name = "user-service")
public interface UserServiceClient {
	@GetMapping("/user/{id}")
	UserResponseDto getUserById(@PathVariable Long id);
}
