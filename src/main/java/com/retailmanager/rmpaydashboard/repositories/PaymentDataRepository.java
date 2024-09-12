package com.retailmanager.rmpaydashboard.repositories;


import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.PaymentData;

public interface PaymentDataRepository extends CrudRepository<PaymentData, Integer> {
    
}
