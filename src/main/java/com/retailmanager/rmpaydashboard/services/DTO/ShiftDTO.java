package com.retailmanager.rmpaydashboard.services.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.retailmanager.rmpaydashboard.models.SaleReport;
import com.retailmanager.rmpaydashboard.models.Shift;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class ShiftDTO {
    // shiftId: Generalmente no se valida en la entrada para creación (es generado).
    // Si fuera para una actualización, podría ser un @NotNull y @Positive.
    // private Long shiftId;
    @Positive(message = "{shift.shiftId.positive}") // Si es para un ID en una operación de actualización
    private Long shiftId; // Para DTOs de respuesta o para actualizaciones donde el ID es necesario

    @NotBlank(message = "{shift.userName.notBlank}")
    @Size(min = 2, max = 100, message = "{shift.userName.size}") // Ajusta min/max según tu necesidad
    private String userName;

    @NotNull(message = "{shift.startTime.notNull}")
    @PastOrPresent(message = "{shift.startTime.pastOrPresent}") // Un turno no puede empezar en el futuro
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    // endTime puede ser nulo si el turno aún está abierto, o NotNull si el DTO representa un turno cerrado
    
    // Si endTime es opcional al crear o actualizar, no pongas @NotNull.
    // Si siempre debe tener un valor al final, podrías ponerlo en un grupo de validación para "cerrar turno".
    @FutureOrPresent(message = "{shift.endTime.futureOrPresent}") // Un turno no puede terminar en el pasado
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotNull(message = "{shift.balanceInicial.notNull}")
    @DecimalMin(value = "0.0", message = "{shift.balanceInicial.decimalMin}")
    @Digits(integer = 19, fraction = 2, message = "{shift.balanceInicial.digits}")
    private BigDecimal balanceInicial;

    //@NotNull(message = "{shift.balanceFinal.notNull}")
    @Digits(integer = 19, fraction = 2, message = "{shift.balanceFinal.digits}")
    private BigDecimal balanceFinal; // Puede ser negativo si el balance es deficitario, por eso no DecimalMin
    @Digits(integer = 19, fraction = 2, message = "{shift.balanceFinal.digits}")
    private BigDecimal cuadreFinal;
    @NotNull // Los booleanos suelen ser no nulos
    private boolean statusShiftBalance;

    // Para validar objetos anidados, usa @Valid y @NotNull
   // @NotNull(message = "{shift.saleReport.notNull}")
    @Valid // Esta anotación asegura que las validaciones dentro de SaleReportDTO también se ejecuten
    private SaleReportDTO saleReport; // Cambiado a SaleReportDTO

    @NotNull(message = "{shift.userId.notNull}")
    @Positive(message = "{shift.userId.positive}") // Asumiendo que userId es el ID de UsersBusiness
    private Long userId;

    @NotBlank(message = "{shift.deviceId.notBlank}")
    private String deviceId;  // Serial del Terminal
    public ShiftDTO(Shift shift) {
        
        if (shift.getSaleReport() != null) {
            this.saleReport=new SaleReportDTO(shift.getSaleReport());
        }
        this.shiftId = shift.getShiftId();
        this.userName = shift.getUserName();
        this.startTime = shift.getStartTime();
        this.endTime = shift.getEndTime();
        this.balanceInicial = shift.getBalanceInicial();
        this.balanceFinal = shift.getBalanceFinal();
        this.cuadreFinal = shift.getCuadreFinal();
        this.statusShiftBalance = shift.isStatusShiftBalance();
        this.userId = shift.getUserBusiness() != null ? shift.getUserBusiness().getUserBusinessId() : null; // Asumiendo que UserBusiness tiene un método getId()
        this.deviceId = shift.getTerminal() != null ? shift.getTerminal().getSerial() : null; // Asumiendo que Terminal tiene un método getSerialNumber()
    }
}
