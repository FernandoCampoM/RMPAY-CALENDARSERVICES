package com.retailmanager.rmpaydashboard.services.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AddressDTO {
    private Long addressId;
    private String street;
    private String town;
    private String zipcode;
}
