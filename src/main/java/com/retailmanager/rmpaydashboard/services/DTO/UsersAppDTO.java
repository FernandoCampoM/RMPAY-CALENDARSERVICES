package com.retailmanager.rmpaydashboard.services.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersAppDTO {

    private Long id;

    @NotBlank(message = "{userapp.username.empty}")
    @Size(min = 5, message = "{userapp.username.min}")
    private String username;

    @NotBlank(message = "{userapp.password.empty}")
    @Size(min = 8, message = "{userapp.password.min}")
    private String password;

    @NotNull(message = "{userapp.active.null}")
    private Boolean active;
}
