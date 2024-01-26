package com.retailmanager.rmpaydashboard.services.DTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AddressDTO {
    private Long addressId;

    @NotBlank(message = "{address.address1.empty}")
    @Size(max = 255, message = "{address.address1.max}")
    private String address1;

    @Size(max = 255, message = "{address.address2.max}")
    private String address2;

    @NotBlank(message = "{address.city.empty}")
    @Size(max = 255, message = "{address.city.max}")
    private String city;

    @NotBlank(message = "{address.country.empty}")
    @Size(max = 255, message = "{address.country.max}")
    private String country;

    @NotBlank(message = "{address.zipcode.empty}")
    @Size(max = 20, message = "{address.zipcode.max}")
    private String zipcode;
}
