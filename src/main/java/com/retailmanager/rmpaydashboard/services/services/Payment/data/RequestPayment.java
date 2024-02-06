
package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor 
public class RequestPayment {
    protected  String  AppKey;
    protected  String  AppType;
    protected  String  mid;
    protected  String  cid;
    protected  String  UserName;
    protected  String  Password;

    public RequestPayment(String AppKey, String AppType, String mid, String cid, String UserName, String Password) {
        this.AppKey = AppKey;
        this.AppType = AppType;
        this.mid = mid;
        this.cid = cid;
        this.UserName = UserName;
        this.Password = Password;
    }
      
}
