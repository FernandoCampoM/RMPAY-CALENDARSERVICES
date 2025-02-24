package com.retailmanager.rmpaydashboard.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.AvailableSchedules;

public interface AvailableSchedulesRepository extends CrudRepository<AvailableSchedules, Long> {

    @Query("SELECT a FROM AvailableSchedules a WHERE a.employee.userBusinessId = :employeeId")
    public Iterable<AvailableSchedules> findByEmployeeId(Long employeeId);

    @Query("SELECT a FROM AvailableSchedules a WHERE a.employee.business.businessId = :prmBusinessId")
    public Iterable<AvailableSchedules> findByBusinessId(Long prmBusinessId);
}
