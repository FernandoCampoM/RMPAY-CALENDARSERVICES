package com.retailmanager.rmpaydashboard.repositories;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.InventoryReceipt;


public interface InventoryReceiptRepository extends CrudRepository<InventoryReceipt, Long>{
    @Query("SELECT i FROM InventoryReceipt i WHERE i.objBusiness.businessId = :businessId ORDER BY i.registerDate desc")
    public Iterable<InventoryReceipt> findByBusiness(Long businessId);

    @Query(value = "SELECT TOP (1) [receiptId] FROM [RMPAY].[dbo].[InventoryReceipt] order by receiptId desc", nativeQuery = true)
    public Long getLastId();
}
