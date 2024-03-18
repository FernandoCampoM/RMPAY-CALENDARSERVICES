package com.retailmanager.rmpaydashboard.services.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TransactionDTO {

    @NotBlank(message = "{transaction.date.notBlank}")
    private String date;

    @NotBlank(message = "{transaction.paymentType.notBlank}")
    private String paymentType;

    @NotNull(message = "{transaction.amount.notNull}")
    private Double amount;

    private String state;

    @NotNull(message = "{transaction.saleId.notNull}")
    private Long saleId;

    private Double changeChash;

    @Size(max = 255, message = "{transaction.refId.size}")
    private String refId = "";

    @Size(max = 255, message = "{transaction.account.size}")
    private String account = "";

    @Size(max = 255, message = "{transaction.cardType.size}")
    private String cardType = "";

    @Size(max = 255, message = "{transaction.authCode.size}")
    private String authCode = "";

    @Size(max = 255, message = "{transaction.batchNo.size}")
    private String batchNo = "";

    @Size(max = 255, message = "{transaction.entryMode.size}")
    private String entryMode = "";

    @Size(max = 255, message = "{transaction.globalUid.size}")
    private String globalUid = "";

    

    // Constructor, getters y setters
}
