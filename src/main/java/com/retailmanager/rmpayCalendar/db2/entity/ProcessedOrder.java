package com.retailmanager.rmpayCalendar.db2.entity;

import java.time.LocalDateTime;

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
}