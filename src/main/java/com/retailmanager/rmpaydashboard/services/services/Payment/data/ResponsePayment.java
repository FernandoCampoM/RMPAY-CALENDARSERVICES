
package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResponsePayment {
    private  String avs;
    private  String cv;
    private  ArrayList<String> Msg;
    private  String msoft_code;
    private  PaymentPlanInfo PaymentPlanInfo;
    private  String phard_code;
    private  int ResponseCode;
    private  String ServiceReferenceNumber;
    private  String Verbiage;
    private  String AuthorizationNumber;
    private  String CardType;
    private  String LastFour;
}
