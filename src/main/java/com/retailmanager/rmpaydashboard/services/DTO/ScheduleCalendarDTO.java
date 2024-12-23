package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDateTime;

import com.retailmanager.rmpaydashboard.models.Business;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScheduleCalendarDTO {
    Long id;
    @NotBlank(message = "{scheduleCalendar.title.empty}")
    String title;
    @NotNull(message = "{scheduleCalendar.start.empty}")
    LocalDateTime dateStart;
    @NotNull(message = "{scheduleCalendar.end.empty}")
    LocalDateTime dateEnd;
    @NotBlank(message = "{scheduleCalendar.color.empty}")
    String color;
   
    Long employeeId;

}
