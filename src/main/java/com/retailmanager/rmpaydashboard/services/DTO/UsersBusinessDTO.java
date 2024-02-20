package com.retailmanager.rmpaydashboard.services.DTO;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersBusinessDTO {

    private Long userBusinessId;

    @NotBlank(message = "{usersbusiness.username.empty}")
    @Size(min = 5, message = "{usersbusiness.username.min}")
    @Size(max = 255, message = "{usersbusiness.username.max}")
    private String username;

    @NotBlank(message = "{usersbusiness.password.empty}")
    @Size(min = 4, message = "{usersbusiness.password.min}")
    private String password;

    @NotNull(message = "{usersbusiness.enable.null}")
    private Boolean enable;
    @NotNull(message = "{usersbusiness.idBusiness.null}")
    @Positive(message = "{usersbusiness.idBusiness.positive}")
    private Long idBusines;
    List<Long> activesPermissions;

    //List<UserPermissionDTO> userPermissions;
}
