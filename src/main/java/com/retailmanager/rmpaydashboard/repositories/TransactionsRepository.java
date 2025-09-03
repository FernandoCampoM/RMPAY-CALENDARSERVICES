package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpaydashboard.models.Transactions;

public interface  TransactionsRepository extends CrudRepository<Transactions, String>{
    @Query( "select t from Transactions t where t.sale.business.businessId=:businessId and t.sale.saleEndDate between :startDate and :endDate")
    public List<Transactions> getTransactionsByRange(@Param("businessId") Long businessId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query( "select t from Transactions t where t.sale.business.merchantId=:merchantId and t.date between :startDate and :endDate")
    public List<Transactions> getTransactionsByMerchantIdAndDateBetween(String merchantId, LocalDateTime startDate, LocalDateTime endDate);
    @Query( "select t from Transactions t where t.sale.business.businessId=:businessId and t.date between :startDate and :endDate")
    public List<Transactions> getTransactionsByBusinessIdAndDateBetween(Long businessId, LocalDateTime startDate, LocalDateTime endDate);
}
