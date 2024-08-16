package com.retailmanager.rmpaydashboard.services.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResellerDTO {
    private Long resellerId;

    @NotNull(message = "{firstname.notnull}")
    @NotEmpty(message = "{firstname.notempty}")
    @Size(min = 3, message = "{firstname.size}")
    private String firstname;

    @NotNull(message = "{lastname.notnull}")
    @NotEmpty(message = "{lastname.notempty}")
    @Size(min = 3, message = "{lastname.size}")
    private String lastname;

    @NotNull(message = "{username.notnull}")
    @NotEmpty(message = "{username.notempty}")
    @Size(min = 3, message = "{username.size}")
    private String username;

    @NotNull(message = "{password.notnull}")
    @NotEmpty(message = "{password.notempty}")
    @Size(min = 3, message = "{password.size}")
    private String password;

    @NotNull(message = "{company.notnull}")
    @NotEmpty(message = "{company.notempty}")
    @Size(min = 3, message = "{company.size}")
    private String company;

    @NotNull(message = "{phone.notnull}")
    @NotEmpty(message = "{phone.notempty}")
    @Size(min = 3, message = "{phone.size}")
    private String phone;

    @NotNull(message = "{address.notnull}")
    @NotEmpty(message = "{address.notempty}")
    @Size(min = 3, message = "{address.size}")
    private String address;

    private boolean status;

    private Long imageprofile;

    private Double commissionsBalance = 0.0;

    private String email1;

    private String email2;
}
