package com.retailmanager.rmpayCalendar.services.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreConfigDTO {
    private Long id;
    private Integer dayOfWeek;
    private Boolean isOpen;
    private String openTime;
    private String closeTime;
}
