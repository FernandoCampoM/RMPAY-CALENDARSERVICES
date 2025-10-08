package com.retailmanager.rmpayCalendar.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpayCalendar.models.ScheduleCalendar;

import jakarta.transaction.Transactional;

public interface ScheduleCalendarRepository extends CrudRepository<ScheduleCalendar, Long> {
    
    @Query("SELECT s FROM ScheduleCalendar s WHERE s.employeeID = :employeeId")
    public Iterable<ScheduleCalendar> findByEmployeeId(Long employeeId);

    /* @Query("SELECT s FROM ScheduleCalendar s WHERE s.employee.business.businessId = :prmBusinessId")
    public Iterable<ScheduleCalendar> findByBusinessId(Long prmBusinessId);
 */    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduleCalendar s WHERE s.employeeID = :employeeId")
    public void deleteByEmployeeId(Long employeeId);
}
