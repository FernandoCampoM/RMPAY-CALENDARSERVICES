package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;

public interface UsersAppRepository extends CrudRepository<UsersBusiness,Long>{
    
    List<UsersBusiness> findByBusiness(Business business);

    Optional<UsersBusiness> findByUsername(String username);

    @Modifying
    @Query("UPDATE UsersBusiness u SET u.enable = :enable WHERE u.userBusinessId = :userBusinessId")
    void updateEnable(Long userBusinessId, boolean enable);
}
