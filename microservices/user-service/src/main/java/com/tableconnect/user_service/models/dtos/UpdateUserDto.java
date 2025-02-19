package com.tableconnect.user_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserDto {
	
	private String name;
	
	private String lastName;
	
    private String email;
    
    private String password;
}
