package com.retailmanager.rmpaydashboard.repositories;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Invoice;

public interface InvoiceRepository extends CrudRepository<Invoice, Long> {
    
}
