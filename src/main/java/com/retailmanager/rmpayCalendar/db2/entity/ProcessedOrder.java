package com.retailmanager.rmpayCalendar.db2.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedOrder {

    @Id
    private String id;

    private LocalDateTime processedAt;
     
     
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String JSONRecibed;
    private String eventType;
    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;
}