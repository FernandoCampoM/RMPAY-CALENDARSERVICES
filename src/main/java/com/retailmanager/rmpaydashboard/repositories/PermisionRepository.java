package com.retailmanager.rmpaydashboard.repositories;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Permission;

public interface PermisionRepository extends CrudRepository<Permission, Long> {
    
}
