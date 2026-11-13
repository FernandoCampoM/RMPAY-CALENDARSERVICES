package com.retailmanager.rmpayCalendar.db2.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpayCalendar.db2.entity.Sys_general_config;

import jakarta.transaction.Transactional;

public interface Sys_general_configRepository extends CrudRepository<Sys_general_config,Long>{
    @Query(value = "SELECT configvalue FROM ShopifyServiceDB.dbo.Sys_general_config WHERE configlabel = 'config.isFirstRun'", nativeQuery = true)
    String getIsFirstRun();

    

    @Modifying
@Transactional
@Query(value = "UPDATE ShopifyServiceDB.dbo.Sys_general_config SET configvalue = :value WHERE configlabel = 'config.isFirstRun'", nativeQuery = true)
void  updateIsFirstRun(@Param("value") String value);
@Query(value = "SELECT configvalue FROM ShopifyServiceDB.dbo.Sys_general_config WHERE configlabel = 'config.lastSyncDate'", nativeQuery = true)
String getLastSyncDate();
@Modifying
@Transactional
@Query(value = "UPDATE ShopifyServiceDB.dbo.Sys_general_config SET configvalue = :value WHERE configlabel = 'config.lastSyncDate'", nativeQuery = true)
void  updateLastSyncDate(@Param("value") String value);
@Modifying
@Transactional
@Query(value = "INSERT INTO ShopifyServiceDB.dbo.Sys_general_config (configlabel, configname, configvalue) VALUES ('config.lastSyncDate', 'LAST SYNC DATE',   (SELECT FORMAT(GETUTCDATE(), 'yyyy-MM-ddTHH:mm:ss') + 'Z'))", nativeQuery = true)
void initializeLastSyncDate();
@Modifying
@Transactional
@Query(value = "INSERT INTO ShopifyServiceDB.dbo.Sys_general_config (configlabel, configname, configvalue) VALUES ('config.isFirstRun', 'IS FIRST RUN', 'true')", nativeQuery = true)
void initializeIsFirstRun();
}
