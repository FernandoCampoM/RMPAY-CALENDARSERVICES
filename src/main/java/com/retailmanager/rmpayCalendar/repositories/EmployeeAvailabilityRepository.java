package com.retailmanager.rmpayCalendar.repositories;

import com.retailmanager.rmpayCalendar.models.EmployeeAvailability;
import org.springframework.data.repository.CrudRepository;

public interface EmployeeAvailabilityRepository extends CrudRepository<EmployeeAvailability, Long> {

    Iterable<EmployeeAvailability> findByEmployeeID(Long employeeId);
}
