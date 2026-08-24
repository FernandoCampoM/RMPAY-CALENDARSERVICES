package com.retailmanager.rmpayCalendar.repositories;

import com.retailmanager.rmpayCalendar.models.StoreConfig;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StoreConfigRepository extends CrudRepository<StoreConfig, Long> {

    Optional<StoreConfig> findByDayOfWeek(Integer dayOfWeek);
}
