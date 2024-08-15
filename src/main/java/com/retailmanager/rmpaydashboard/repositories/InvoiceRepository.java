package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Invoice;

public interface InvoiceRepository extends CrudRepository<Invoice, Long> {

    public List<Invoice> findByBusinessIdAndDateGreaterThanEqualAndDateLessThanEqualOrderByInvoiceNumberDesc(Long businessId, LocalDate startDate, LocalDate endDate);


    @Query("Select i from Invoice i  where i.date>=:startDate AND i.date<=:endDate")
    public List<Invoice> getPaymentReports(LocalDate startDate, LocalDate endDate);
}
