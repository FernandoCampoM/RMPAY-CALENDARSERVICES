package com.retailmanager.rmpayCalendar.db2.entity;

import java.math.BigDecimal;

import lombok.Data;


@Data
public class PosProduct {

    private String productCode;
    private String productName;
    private int currentStock;
    private BigDecimal price;
    private BigDecimal cost;
    private String barCode;
    private String barCode2;
    private String category;
    private String department;
}

