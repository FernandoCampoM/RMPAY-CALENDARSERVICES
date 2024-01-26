package com.retailmanager.rmpaydashboard.services.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PermissionDTO {
    private Long permissionId;

    @NotBlank(message = "{permission.name.empty}")
    @Size(max = 255, message = "{permission.name.max}")
    private String name;

}
