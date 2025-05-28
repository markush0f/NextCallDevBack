package com.tableconnect.user_service.services;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.tableconnect.user_service.controllers.UserController;
import com.tableconnect.user_service.exceptions.DatabaseException;
import com.tableconnect.user_service.exceptions.InternalServerErrorException;
import com.tableconnect.user_service.exceptions.UserAlreadyExistsException;
import com.tableconnect.user_service.exceptions.UserNotFoundException;
import com.tableconnect.user_service.mappers.IUserMapper;
import com.tableconnect.user_service.models.dtos.CreateUserDto;
import com.tableconnect.user_service.models.dtos.UpdateUserDto;
import com.tableconnect.user_service.models.dtos.UserResponseDto;
import com.tableconnect.user_service.models.entities.User;
import com.tableconnect.user_service.repositories.UserRepository;

@Service
public class UserServiceImpl implements IUserService{

	private UserRepository userRepository;
	
	private IUserMapper userMapper;
	
    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	
	public UserServiceImpl(UserRepository userRepository, IUserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
	
	@Override
	public UserResponseDto getUserById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(
						() -> new UserNotFoundException("User with id " + id + " not found")
                );
		return userMapper.userToUserResponseDto(user);
	}

	@Override
	public UserResponseDto createUser(CreateUserDto createUserDto) {
	    try {
	        User user = userMapper.createUserDtoToUser(createUserDto);
	        checkIfUserExistsByEmail(user.getEmail());
	        User userSaved = userRepository.save(user);
	        return userMapper.userToUserResponseDto(userSaved);
	    } catch (UserAlreadyExistsException e) {
	        throw e;  
	    } catch (DataAccessException e) {
	        throw new DatabaseException("Error accessing the database", e); 
	    } catch (InternalServerErrorException e) {
	        throw new InternalServerErrorException("Error creating the user", e); 
	    }
	}

	
	@Override
	public void deleteUser(Long id) {
		try {
			userRepository.findById(id)
			.orElseThrow(
					() -> new UserNotFoundException("User with id " + id + " not found")
            );
			userRepository.deleteById(id);
		}catch (InternalServerErrorException e) {
		    throw new InternalServerErrorException("Error removing the user", e);  
		}
	}

	@Override
	public UserResponseDto updateUser(Long id, UpdateUserDto updateUserDto) {
	    try {
	    	User user = userRepository.findById(id)
	    			.orElseThrow(()-> 
	    			new UserNotFoundException("User with id " + id + " not found"));
	    	
	    	User userUpdated = userMapper.updateUserDtoToUser(updateUserDto, user); 
	        userRepository.save(userUpdated);
	            
	        logger.info("User updated {},",userUpdated);
	            
	        return userMapper.userToUserResponseDto(user);
	    } catch (Exception e) {
	        throw new InternalServerErrorException("Error updating user", e);
	    }
	}

	@Override
	public List<UserResponseDto> getAllUsers() {
	    List<User> users = (List<User>) userRepository.findAll();
	    return users.stream()
	    		.map(userMapper::userToUserResponseDto)
                    .collect(Collectors.toList());
	}

	
	private void checkIfUserExistsByEmail(String email) {
		if (userRepository.findByEmail(email).isPresent()) {
			throw new UserAlreadyExistsException("El email ya está en uso: " + email);
		}
	}



}
