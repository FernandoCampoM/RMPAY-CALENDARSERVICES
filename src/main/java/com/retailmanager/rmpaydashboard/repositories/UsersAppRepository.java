package com.retailmanager.rmpaydashboard.repositories;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.UsersBusiness;

public interface UsersAppRepository extends CrudRepository<UsersBusiness,Long>{
    
}
