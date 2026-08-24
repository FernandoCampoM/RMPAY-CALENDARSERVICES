package com.retailmanager.rmpayCalendar.services.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffingRequirementDTO {
    private Long id;
    @NotBlank
    private String startTime;
    @NotBlank
    private String endTime;
    private Integer monRequired;
    private Integer tueRequired;
    private Integer wedRequired;
    private Integer thuRequired;
    private Integer friRequired;
    private Integer satRequired;
    private Integer sunRequired;
}
