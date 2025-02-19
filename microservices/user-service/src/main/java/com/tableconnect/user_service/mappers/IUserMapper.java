package com.tableconnect.user_service.mappers;

import com.tableconnect.user_service.models.dtos.CreateUserDto;
import com.tableconnect.user_service.models.dtos.UpdateUserDto;
import com.tableconnect.user_service.models.dtos.UserResponseDto;
import com.tableconnect.user_service.models.entities.User;

public interface IUserMapper {

	User createUserDtoToUser(CreateUserDto createUserDto);
	
	UserResponseDto userToUserResponseDto(User user);
	
	User updateUserDtoToUser(UpdateUserDto updateUserDto, User user);
}
