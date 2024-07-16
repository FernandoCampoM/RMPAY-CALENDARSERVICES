package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResponseJSON {
    private String Token;
    private String Identifier;
    private String msoft_code;
    private String PhardCode;
    private String Verbiage;
    private int ResponseCode;
    private String Msg;
}
