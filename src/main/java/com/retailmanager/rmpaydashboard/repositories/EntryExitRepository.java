package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.EntryExit;

public interface EntryExitRepository extends CrudRepository<EntryExit, Long> {
    
    @Query("SELECT e FROM EntryExit e WHERE e.userBusiness.userBusinessId = :userBusinessId ORDER BY e.date DESC, e.hour DESC")
    public List<EntryExit> findByUserBusinessId(Long userBusinessId);
}
