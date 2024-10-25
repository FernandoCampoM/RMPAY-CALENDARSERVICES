package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.ResellerPayment;

public interface ResellerPaymentRepository  extends CrudRepository<ResellerPayment,Long> {

    @Query("SELECT s FROM ResellerPayment s WHERE s.reseller.resellerId = :resellerId ORDER BY s.paymentId DESC")
    public List<ResellerPayment> getAllBy(Long resellerId);
    @Query("SELECT s FROM ResellerPayment s WHERE s.reseller.resellerId = :resellerId AND s.date BETWEEN :startDate AND :endDate ORDER BY s.paymentId DESC")
    public List<ResellerPayment> getAllBy(Long resellerId,LocalDate startDate,LocalDate endDate);
}
