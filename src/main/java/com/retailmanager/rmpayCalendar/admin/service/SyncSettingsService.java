package com.retailmanager.rmpayCalendar.admin.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpayCalendar.db2.entity.Sys_general_config;
import com.retailmanager.rmpayCalendar.db2.repository.AdminConfigRepository;
import com.retailmanager.rmpayCalendar.enums.SyncMaster;

/**
 * Parámetros de sincronización editables desde el panel admin, guardados en Sys_general_config.
 * Depende de: AdminConfigRepository (lectura/escritura por configlabel).
 * No modifica: config.isFirstRun ni config.lastSyncDate.
 */
@Service
public class SyncSettingsService {

    public static final String MASTER = "sync.master";
    public static final String CREATE_ENABLED = "sync.createEnabled";
    public static final String IMPORT_DRAFTS = "sync.importDrafts";
    public static final String MAX_DELETE_PERCENT = "sync.maxDeletePercent";

    @Autowired
    private AdminConfigRepository repository;

    public SyncMaster getMaster() {
        try {
            return SyncMaster.valueOf(get(MASTER, SyncMaster.RM.name()));
        } catch (IllegalArgumentException e) {
            return SyncMaster.RM;
        }
    }

    /** Las altas automáticas quedan bloqueadas hasta que se cierre la conciliación. */
    public boolean isCreateEnabled() {
        return Boolean.parseBoolean(get(CREATE_ENABLED, "false"));
    }

    public boolean isImportDrafts() {
        return Boolean.parseBoolean(get(IMPORT_DRAFTS, "false"));
    }

    public int getMaxDeletePercent() {
        try {
            return Integer.parseInt(get(MAX_DELETE_PERCENT, "10"));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("master", getMaster().name());
        map.put("createEnabled", isCreateEnabled());
        map.put("importDrafts", isImportDrafts());
        map.put("maxDeletePercent", getMaxDeletePercent());
        return map;
    }

    public String get(String label, String defaultValue) {
        Sys_general_config config = repository.findFirstByConfiglabel(label);
        return config == null || config.getConfigvalue() == null ? defaultValue : config.getConfigvalue();
    }

    public void set(String label, String name, String value) {
        Sys_general_config config = repository.findFirstByConfiglabel(label);
        if (config == null) {
            config = new Sys_general_config();
            config.setConfiglabel(label);
            config.setConfigname(name);
        }
        config.setConfigvalue(value);
        repository.save(config);
    }
}
