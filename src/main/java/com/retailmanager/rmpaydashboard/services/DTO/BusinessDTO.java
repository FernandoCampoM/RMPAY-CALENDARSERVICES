package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor  @AllArgsConstructor
public class BusinessDTO {
    private Long businessId;

    private String merchantId;
    @NotBlank(message = "{business.name.empty}")
    @Size(max = 255, message = "{business.name.max}")
    private String name;

    @NotBlank(message = "{business.businessPhoneNumber.empty}")
    @Size(max = 20, message = "{business.businessPhoneNumber.max}")
    private String businessPhoneNumber;

    @NotNull(message = "{business.additionalTerminals.null}")
    @Min(value = 0, message = "{business.additionalTerminals.min}")
    private Integer additionalTerminals;

    private Long serviceId;
    private double discount=0.0;
    private boolean enable=false;
    private boolean terms=false;
    private LocalDate lastPayment;
    private String comment;
    private String logo;
    private String logoAth;
    private LocalDate registerDate;
    @Valid
    private AddressDTO address;
    private UserDTO user;

    @NotNull(message = "{business.userId.null}")
    private Long userId;
    private List<CategoryDTO> categories;
    
    private List<TerminalDTO> terminals;
}
