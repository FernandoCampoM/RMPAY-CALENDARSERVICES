package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpaydashboard.models.Transactions;

public interface  TransactionsRepository extends CrudRepository<Transactions, Long>{
    @Query( "select t from Transactions t where t.sale.business.businessId=:businessId and t.sale.saleEndDate between :startDate and :endDate")
    public List<Transactions> getTransactionsByRange(@Param("businessId") Long businessId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
