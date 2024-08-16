package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class ResellerPaymentDTO {
    private Integer paymentId = 0;
    private LocalDate date;
    private LocalTime time;
    private String paymentMethod;
    private Double total;
    private Integer resellerId;
    private List<Integer> resellerSalesId = new ArrayList<>();
}
