package com.retailmanager.rmpaydashboard.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.retailmanager.rmpaydashboard.models.Shift;

public interface ShiftReporsitory extends CrudRepository<Shift, Long>,  PagingAndSortingRepository<Shift, Long> {
    @Query("select s from Shift s where s.userBusiness.userBusinessId = :userBusinessId and s.terminal.terminalId = :terminalId")
    public Shift findByEmployeeAndTerminal(Long userBusinessId, String terminalId);
}
