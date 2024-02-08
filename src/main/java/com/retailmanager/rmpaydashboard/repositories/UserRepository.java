package com.retailmanager.rmpaydashboard.repositories;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.User;

public interface UserRepository extends  CrudRepository<User,Long>{
    Optional<User> findOneByUsername(String username);
    /**
     * Update the enable status for a specific user.
     *
     * @param  userID  the ID of the user to update
     * @param  enable  the new enable status
     */
    @Modifying
    @Query("UPDATE User u SET u.enable = :enable WHERE u.userID = :userID")
    void updateEnable(Long userID, boolean enable);
    
    Optional<User> findOneByEmail(String username);
}
