package com.retailmanager.rmpaydashboard.repositories;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.UserPermission;

public interface UserPermissionRepository extends CrudRepository<UserPermission, Long> {
    
}
