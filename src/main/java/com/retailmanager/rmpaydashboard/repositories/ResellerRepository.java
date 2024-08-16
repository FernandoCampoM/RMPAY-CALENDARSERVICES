package com.retailmanager.rmpaydashboard.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Reseller;

public interface ResellerRepository  extends CrudRepository<Reseller,Long> {
    Optional<Reseller> findByUsername(String username);
}
