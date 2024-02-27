package com.retailmanager.rmpaydashboard.services.DTO;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuyTerminalDTO {
    
    private Long idService;
    private Long businesId;

    //INFORMACIÓN DEL PAGO
    @NotNull(message = "{registry.automaticPayments.null}")
    private boolean automaticPayments;
    
    private String paymethod="";
    //PARA PAGO CON TARJETA
    private String nameoncard;
    private String creditcarnumber;
    private String securitycode;    
    private String cardType;        
    private String expDateMonth;
    private String expDateYear;
    
    //PARA PAGO CON CHEQUE
    private String accountNameBank;
    private String accountNumberBank;
    private String routeNumberBank;
    private Long chequeVoidId;
}
