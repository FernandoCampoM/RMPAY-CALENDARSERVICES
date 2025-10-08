package com.retailmanager.rmpayCalendar.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpayCalendar.models.AvailableSchedules;

import jakarta.transaction.Transactional;

public interface AvailableSchedulesRepository extends CrudRepository<AvailableSchedules, Long> {

    @Query("SELECT a FROM AvailableSchedules a WHERE a.employeeID = :employeeId")
    public Iterable<AvailableSchedules> findByEmployeeId(Long employeeId);

    /* @Query("SELECT a FROM AvailableSchedules a WHERE a.employee.business.businessId = :prmBusinessId")
    public Iterable<AvailableSchedules> findByBusinessId(Long prmBusinessId); */

    @Modifying
    @Transactional
    @Query("DELETE FROM AvailableSchedules a WHERE a.employeeID = :employeeId")
    public void deleteByEmployeeId(Long employeeId);
}
