package com.retailmanager.rmpaydashboard.services.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class ServiceDTO {

    private Long serviceId;

    @NotBlank(message = "{service.serviceName.empty}")
    private String serviceName;

    private String serviceDescription;

    @NotNull(message = "{service.serviceValue.null}")
    @Positive(message = "{service.serviceValue.positive}")
    private Double serviceValue;

    @NotNull(message = "{service.enable.null}")
    private Boolean enable;

    private Double referralPayment;

    @NotNull(message = "{service.duration.null}")
    @Positive(message = "{service.duration.positive}")
    private Integer duration;
    @NotNull(message = "{service.terminals2to5.null}")
    @Positive(message = "{service.terminals2to5.positive}")
    private Double terminals2to5;

    @NotNull(message = "{service.referralPayment2to5.null}")
    @Positive(message = "{service.referralPayment2to5.positive}")
    private Double referralPayment2to5;

    @NotNull(message = "{service.terminals6to9.null}")
    @Positive(message = "{service.terminals6to9.positive}")
    private Double terminals6to9;

    @NotNull(message = "{service.referralPayment6to9.null}")
    @Positive(message = "{service.referralPayment6to9.positive}")
    private Double referralPayment6to9;

    @NotNull(message = "{service.terminals10.null}")
    @Positive(message = "{service.terminals10.positive}")
    private Double terminals10;

    @NotNull(message = "{service.referralPayment10.null}")
    @Positive(message = "{service.referralPayment10.positive}")
    private Double referralPayment10;
}