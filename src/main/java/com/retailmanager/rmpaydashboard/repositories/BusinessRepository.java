package com.retailmanager.rmpaydashboard.repositories;

import com.retailmanager.rmpaydashboard.models.Business;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface BusinessRepository extends CrudRepository<Business,Long> {
    Optional<Business> findOneByMerchantId(String merchantId);
}
