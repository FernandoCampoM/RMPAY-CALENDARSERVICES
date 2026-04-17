package com.retailmanager.rmpayCalendar.db2.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class PosProduct {

    @JsonProperty("ProductCode")
    private String productCode;

    @JsonProperty("ProductName")
    private String productName;

    @JsonProperty("CurrentStock")
    private int currentStock;

    @JsonProperty("Price")
    private BigDecimal price;

    @JsonProperty("Cost")
    private BigDecimal cost;

    @JsonProperty("BarCode")
    private String barCode;

    @JsonProperty("BarCode2")
    private String barCode2;

    @JsonProperty("WebDesc")
    private String webDesc;

    @JsonProperty("Category")
    private String category;

    @JsonProperty("Department")
    private String department;
}

