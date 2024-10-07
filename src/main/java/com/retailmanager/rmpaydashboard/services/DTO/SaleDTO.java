package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SaleDTO {

    private Long saleID;


    @NotBlank(message = "{saleCreationDate.notBlank}")
    private String saleCreationDate;

    private LocalDate saleEndDate;

    private String items;

    @NotNull(message = "{saleSubtotal.notNull}")
    @PositiveOrZero(message = "{saleSubtotal.positiveOrZero}")
    private Double saleSubtotal;

    @NotNull(message = "{saleStateTaxAmount.notNull}")
    @PositiveOrZero(message = "{saleStateTaxAmount.positiveOrZero}")
    private Double saleStateTaxAmount;

    @NotNull(message = "{saleCityTaxAmount.notNull}")
    @PositiveOrZero(message = "{saleCityTaxAmount.positiveOrZero}")
    private Double saleCityTaxAmount;

    @NotNull(message = "{saleReduceTax.notNull}")
    @PositiveOrZero(message = "{saleReduceTax.positiveOrZero}")
    private Double saleReduceTax;

    @NotNull(message = "{saleTotalAmount.notNull}")
    @PositiveOrZero(message = "{saleTotalAmount.positiveOrZero}")
    private Double saleTotalAmount;

    @NotBlank(message = "{saleTransactionType.notBlank}")
    private String saleTransactionType;

    private Integer saleMachineID;

    private String saleIvuNumber;

    private String saleStatus;

    private Double saleChange;

    @NotNull(message = "{userId.notNull}")
    @Positive(message = "{userId.positive}")
    private Integer userId;

    //@NotBlank(message = "{merchantId.notBlank}")
    //private String merchantId;
    @PositiveOrZero(message = "{sale.businessId.notBlank}")
    private Long businessId;
    private Integer saleToRefund;

    private String terminalId;
    @NotNull(message = "{sale.tipAmount.notNull}")
    @PositiveOrZero(message = "{sale.tipAmount.positiveOrZero}")
    private Double tipAmount;
}
