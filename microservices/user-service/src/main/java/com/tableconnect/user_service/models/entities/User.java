package com.tableconnect.user_service.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
public class User {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
	private Long id;
	
	private String name;
	
	@Column(name = "last_name"  , nullable = false )
	private String lastName;
	
	@Column(unique = true)
	private String email;
	
	private String password;
	
}
