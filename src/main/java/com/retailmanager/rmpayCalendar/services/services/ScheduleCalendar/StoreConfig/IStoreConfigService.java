package com.retailmanager.rmpayCalendar.services.services.ScheduleCalendar.StoreConfig;

import com.retailmanager.rmpayCalendar.services.DTO.StoreConfigDTO;
import org.springframework.http.ResponseEntity;

public interface IStoreConfigService {
    ResponseEntity<?> save(StoreConfigDTO dto);
    ResponseEntity<?> update(Long id, StoreConfigDTO dto);
    boolean delete(Long id);
    ResponseEntity<?> findById(Long id);
    ResponseEntity<?> getAll();
}
