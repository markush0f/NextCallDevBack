package com.tableconnect.user_service.mappers;

import org.springframework.stereotype.Component;

import com.tableconnect.user_service.models.dtos.CreateUserDto;
import com.tableconnect.user_service.models.dtos.UpdateUserDto;
import com.tableconnect.user_service.models.dtos.UserResponseDto;
import com.tableconnect.user_service.models.entities.User;

@Component
public class UserMapper implements IUserMapper{

	@Override
	public User createUserDtoToUser(CreateUserDto createUserDto) {
		
		return User.builder()
				.name(createUserDto.getName())
				.lastName(createUserDto.getLastName())
				.email(createUserDto.getEmail())
				.password(createUserDto.getPassword())
				.build();
	}

	@Override
	public UserResponseDto userToUserResponseDto(User user) {
		return UserResponseDto.builder()
				.id(user.getId())
				.name(user.getName())
				.lastName(user.getLastName())
				.email(user.getEmail())
				.build();
	}

	@Override
	public User updateUserDtoToUser(UpdateUserDto updateUserDto, User user) {
		return User.builder()
				.id(user.getId())
				.name(updateUserDto.getName() == null? user.getName() : updateUserDto.getName())
                .lastName(updateUserDto.getLastName() == null? user.getLastName() : updateUserDto.getLastName())
                .email(updateUserDto.getEmail() == null? user.getEmail() : updateUserDto.getEmail())
                .password(user.getPassword())
                .build();
	}

	
}
