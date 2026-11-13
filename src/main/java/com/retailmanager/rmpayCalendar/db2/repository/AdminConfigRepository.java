package com.retailmanager.rmpayCalendar.db2.repository;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpayCalendar.db2.entity.Sys_general_config;

/**
 * Acceso genérico por etiqueta a Sys_general_config para los parámetros del panel admin.
 * Convive con Sys_general_configRepository, que no se modifica.
 */
public interface AdminConfigRepository extends CrudRepository<Sys_general_config, Long> {

    Sys_general_config findFirstByConfiglabel(String configlabel);
}
