package com.retailmanager.rmpaydashboard.repositories;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

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
    /**
     * Update the last login date for a specific user.
     *
     * @param  userID    the ID of the user
     * @param  localDate the new last login date
     * @return           void
     */
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :localDate WHERE u.userID = :userID")
    void updateLastLogin(Long userID, LocalDate localDate);
    
    Optional<User> findOneByEmail(String username);
    @Query("SELECT u FROM User u WHERE u.enable = true and ELEMENT(u.business).additionalTerminals  > 0")
    public List<User> findActives();
    /**
     * Obtiene una lista de usuarios por la fecha de último inicio de sesión 
     * menor a una fecha dada.
     *
     * @param  date	fecha de último inicio de sesión dada
     * @return         listado de usuarios
     */
    //
    public List<User> findByLastLoginIsNullOrLastLoginLessThan(LocalDate date);

    

}
