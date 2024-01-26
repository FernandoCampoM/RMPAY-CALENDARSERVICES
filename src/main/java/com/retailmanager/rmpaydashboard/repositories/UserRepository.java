package com.retailmanager.rmpaydashboard.repositories;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.User;

public interface UserRepository extends  CrudRepository<User,Long>{
    Optional<User> findOneByUsername(String username);
    @Modifying
    @Query("UPDATE User u SET u.enable = :enable WHERE u.userId = :userId")
    void updateEnable(Long userId, boolean enable);
}
