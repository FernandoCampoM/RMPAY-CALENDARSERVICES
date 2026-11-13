package com.retailmanager.rmpayCalendar.db2.entity;

import com.retailmanager.rmpayCalendar.enums.SyncAction;

import lombok.Data;

@Data
public class ProductSyncEvent {

    private String productCode;
    private PosProduct product;
    private SyncAction action; // CREATE o UPDATE
    private String hash;
}
