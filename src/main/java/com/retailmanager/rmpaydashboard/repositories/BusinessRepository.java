package com.retailmanager.rmpaydashboard.repositories;

import com.retailmanager.rmpaydashboard.models.Business;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface BusinessRepository extends CrudRepository<Business,Long> {
    Optional<Business> findOneByMerchantId(String merchantId);
    @Modifying
    @Query("UPDATE Business u SET u.enable = :enable WHERE u.businessId = :businessId")
    void updateEnable(Long businessId, boolean enable);
}
