package com.retailmanager.rmpaydashboard.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.rmpayAtTheTable.RMPayAtTheTable_User;


public interface UserPayAtTableRepository  extends CrudRepository<RMPayAtTheTable_User, Long>{

    Optional<RMPayAtTheTable_User> findByUsername(String username);
    @Query("SELECT u FROM RMPayAtTheTable_User u WHERE u.merchantId = :merchantId")
    Optional<RMPayAtTheTable_User> findByMerchantId(String merchantId);

    @Query("""
    SELECT DISTINCT u FROM RMPayAtTheTable_User u
    LEFT JOIN u.terminals t
    WHERE u.businessName LIKE :filter 
    OR u.address LIKE :filter 
    OR u.merchantId LIKE :filter 
    OR u.name LIKE :filter 
    OR u.username LIKE :filter 
    OR u.phone LIKE :filter 
    OR t.serialNumber LIKE :filter
""")
    public Page<RMPayAtTheTable_User> findyAllUsersByFilter(Pageable pageable, String filter);
    @Query("SELECT b FROM RMPayAtTheTable_User b ")
    public Page<RMPayAtTheTable_User> findyAllUsersByFilter(Pageable pageable);
}
