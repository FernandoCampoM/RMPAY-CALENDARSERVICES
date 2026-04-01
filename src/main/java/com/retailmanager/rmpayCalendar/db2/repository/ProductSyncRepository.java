package com.retailmanager.rmpayCalendar.db2.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;

public interface ProductSyncRepository extends CrudRepository<ProductSync, Long> {
    public ProductSync findByProductCode(String productCode);
    public List<ProductSync> findAllByLastSeenBefore( LocalDateTime limit );
    
}
