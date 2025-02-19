package com.tableconnect.user_service.services;

import java.util.List;

import com.tableconnect.user_service.models.dtos.CreateUserDto;
import com.tableconnect.user_service.models.dtos.UpdateUserDto;
import com.tableconnect.user_service.models.dtos.UserResponseDto;

public interface IUserService {
	public UserResponseDto getUserById(Long id);	
	
	public UserResponseDto createUser(CreateUserDto createUserDto);
	
	public void deleteUser(Long id);
	
	public UserResponseDto updateUser(Long id, UpdateUserDto updateUserDto);
	
	public List<UserResponseDto> getAllUsers();
}
