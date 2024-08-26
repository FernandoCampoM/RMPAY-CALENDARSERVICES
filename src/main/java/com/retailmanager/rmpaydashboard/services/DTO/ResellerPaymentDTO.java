package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ResellerPaymentDTO {
    private Long paymentId ;
    private LocalDate date;
    private LocalTime time;
    private String paymentMethod;
    private Double total;
    private Long resellerId;
    private List<Long> resellerSalesId = new ArrayList<>();
    private List<HashMap<String,String>> salesInfo = new ArrayList<>();
}
