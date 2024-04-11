package com.retailmanager.rmpaydashboard.services.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDTO {

    @NotBlank(message = "{payment.method.code.notBlank}")
    @Size(max = 255, message = "{payment.method.code.size}")
    private String code;

    @NotBlank(message = "{payment.method.name.notBlank}")
    @Size(max = 255, message = "{payment.method.name.size}")
    private String name;

    @NotBlank(message = "{payment.method.enable.notBlank}")
    private Boolean enable;

    @Size(max = 1000, message = "{payment.method.notes.size}")
    private String notes;
}
