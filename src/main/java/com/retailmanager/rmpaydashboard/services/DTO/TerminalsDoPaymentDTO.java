package com.retailmanager.rmpaydashboard.services.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor  @AllArgsConstructor
public class TerminalsDoPaymentDTO {
    private String terminalId;
    private Long idService;

    //ATRIUBUTOS DE PAGO INTERNOS QUE NO SE RECIBEN DESDE EL FRONTEND
    private Double amount;
    private String serviceDescription;
    private boolean isPrincipal;
}
