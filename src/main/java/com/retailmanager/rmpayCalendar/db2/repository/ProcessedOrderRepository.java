package com.retailmanager.rmpayCalendar.db2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpayCalendar.db2.entity.ProcessedOrder;
public interface ProcessedOrderRepository extends CrudRepository<ProcessedOrder, String>{
    
}
