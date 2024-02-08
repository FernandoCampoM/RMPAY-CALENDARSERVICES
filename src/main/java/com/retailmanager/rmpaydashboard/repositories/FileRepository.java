package com.retailmanager.rmpaydashboard.repositories;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.FileModel;

public interface FileRepository extends CrudRepository<FileModel, Long> {
    
}
