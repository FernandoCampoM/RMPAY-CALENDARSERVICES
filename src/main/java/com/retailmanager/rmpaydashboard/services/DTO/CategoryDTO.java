package com.retailmanager.rmpaydashboard.services.DTO;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long categoryId;

    @NotBlank(message = "{category.name.empty}")
    private String name;
    private Long businesId;
    @NotNull(message = "{category.enable.null}")
    private Boolean enable;
}
