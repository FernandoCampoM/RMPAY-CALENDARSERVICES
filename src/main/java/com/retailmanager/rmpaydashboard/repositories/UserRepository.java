package com.retailmanager.rmpaydashboard.repositories;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.User;

public interface UserRepository extends  CrudRepository<User,Long>{
    Optional<User> findOneByUsername(String username);
}
