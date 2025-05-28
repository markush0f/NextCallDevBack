package com.tableconnect.user_service.repositories;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import com.tableconnect.user_service.models.entities.User;


public interface UserRepository extends CrudRepository<User, Long>{
	public Optional<User> findByEmail(String email);
}
