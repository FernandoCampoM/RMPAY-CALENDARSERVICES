package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Sale;

public interface SaleRepository extends CrudRepository<Sale, Long>  {
    
    public List<Sale> findByMerchantId(String merchantId);
    public List<Sale> findBySaleTransactionTypeAndSaleStatusAndMerchantId(String saleTransactionType, String saleStatus, String merchantId);
    public List<Sale> findBySaleTransactionTypeAndMerchantId(String saleTransactionType, String merchantId);

    public List<Sale> findBySaleEndDateBetweenAndSaleTransactionTypeAndSaleStatusAndMerchantId(LocalDate startDate, LocalDate endDate, String saleTransactionType, String saleStatus, String merchantId);
}
