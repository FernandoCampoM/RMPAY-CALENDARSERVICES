package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.ResellerPayment;
import com.retailmanager.rmpaydashboard.models.ResellerSales;

public interface ResellerSalesRepository extends CrudRepository<ResellerSales,Long> {
    @Query("SELECT s FROM ResellerSales s ORDER BY s.reseller.username, s.resellerSalesId DESC")
    public List<ResellerSales> getAllResellers();
    @Query("SELECT s FROM ResellerSales s WHERE s.reseller.resellerId = :resellerId ORDER BY s.resellerSalesId DESC")
    public List<ResellerSales> getAllBy(Long resellerId);
    @Query("SELECT s FROM ResellerSales s WHERE s.reseller.resellerId = :resellerId AND s.resellerPayment IS NULL ORDER BY s.resellerSalesId DESC")
    public List<ResellerSales> getUnpaidAccounts(Long resellerId);
    @Query("UpDATE ResellerSales SET resellerPayment = :resellerPayment, paymentDate = :paymentDate WHERE resellerSalesId = :resellerSalesId")
    @Modifying
    public int UpdatePaymentDateAndResellerPayment(Long resellerSalesId,LocalDate paymentDate,ResellerPayment resellerPayment);
}
