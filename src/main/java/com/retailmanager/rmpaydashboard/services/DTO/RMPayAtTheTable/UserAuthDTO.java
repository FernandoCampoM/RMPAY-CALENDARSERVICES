package com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO {
    
    @NotBlank(message = "{userAuth.username.notBlank}")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{userAuth.username.pattern}")
    private String username;
    
    @NotBlank(message = "{userAuth.password.notBlank}")
    //@Size(min = 8, max = 20, message = "{userAuth.password.size}")
    //@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
      //       message = "{userAuth.password.pattern}")
    private String password;

    @NotBlank(message = "{userAuth.serialNumber.notBlank}")
    //@Pattern(regexp = "^[A-Za-z0-9-]{5,50}$", message = "{userAuth.serialNumber.pattern}")
    private String serialNumber;
}
