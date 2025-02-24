package com.retailmanager.rmpaydashboard.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.ScheduleCalendar;

public interface ScheduleCalendarRepository extends CrudRepository<ScheduleCalendar, Long> {
    
    @Query("SELECT s FROM ScheduleCalendar s WHERE s.employee.userBusinessId = :employeeId")
    public Iterable<ScheduleCalendar> findByEmployeeId(Long employeeId);

    @Query("SELECT s FROM ScheduleCalendar s WHERE s.employee.business.businessId = :prmBusinessId")
    public Iterable<ScheduleCalendar> findByBusinessId(Long prmBusinessId);
}
