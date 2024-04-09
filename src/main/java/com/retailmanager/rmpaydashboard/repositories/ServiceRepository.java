package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Service;

public interface ServiceRepository extends CrudRepository<Service, Long> {
    @Modifying
    @Query("UPDATE Service u SET u.enable = :enable WHERE u.serviceId = :serviceId")
    void updateEnable(Long serviceId, boolean enable);

    public List<Service> findByEnable(boolean enable);
}
