package com.tableconnect.user_service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tableconnect.user_service.models.dtos.CreateUserDto;
import com.tableconnect.user_service.models.dtos.UpdateUserDto;
import com.tableconnect.user_service.models.dtos.UserResponseDto;
import com.tableconnect.user_service.models.entities.User;
import com.tableconnect.user_service.services.IUserService;

@RestController()
@RequestMapping("/user")
public class UserController {
	
    Logger logger = LoggerFactory.getLogger(UserController.class);

	
	private IUserService userService;
	
	public UserController(IUserService userService) {
        this.userService = userService;
    }
	
	@GetMapping("{id}")
	public ResponseEntity<?> getUserById(@PathVariable() Long id){
		UserResponseDto user = userService.getUserById(id);	
		return ResponseEntity.ok(user);
	}
	
	@PostMapping()
	public ResponseEntity<?> createUser(@RequestBody() CreateUserDto createUserDto){
		logger.info("CreateUserDto received: {}",createUserDto);
		UserResponseDto userResponseDto = userService.createUser(createUserDto);    
		return ResponseEntity.ok(userResponseDto);    
	}
	
	@DeleteMapping("{id}")
	public ResponseEntity<?> deleteUser(@PathVariable() Long id){
		logger.info("Id recived: {}",id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
	
	@PutMapping("{id}")
	public ResponseEntity<?> updateUser(@PathVariable() Long id, @RequestBody() UpdateUserDto updateUserDto){
        UserResponseDto userResponseDto = userService.updateUser(id, updateUserDto);
        return ResponseEntity.ok(userResponseDto);
    }
	
	@GetMapping()
	public ResponseEntity<?> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }
	
	
}
