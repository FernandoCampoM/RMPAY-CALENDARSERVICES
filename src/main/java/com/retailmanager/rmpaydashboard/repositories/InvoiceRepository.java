package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Invoice;

public interface InvoiceRepository extends CrudRepository<Invoice, Long> {

    public List<Invoice> findByBusinessIdAndDateGreaterThanEqualAndDateLessThanEqual(Long businessId, LocalDate startDate, LocalDate endDate);
}
