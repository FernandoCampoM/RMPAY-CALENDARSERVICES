package com.retailmanager.rmpaydashboard.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.rmpayAtTheTable.RMPayAtTheTable_User;


public interface UserPayAtTableRepository  extends CrudRepository<RMPayAtTheTable_User, Long>{

    Optional<RMPayAtTheTable_User> findByUsername(String username);
    @Query("SELECT u FROM RMPayAtTheTable_User u WHERE u.merchantId = :merchantId")
    Optional<RMPayAtTheTable_User> findByMerchantId(String merchantId);
}
