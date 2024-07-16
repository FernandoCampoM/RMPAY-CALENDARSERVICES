
package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ReqBlackStoneData {
    private  String url;
    private  String urlForToken;
    private  String urlForPymentWithToken;
    private  String  AppKey;
    private  String  AppType;
    private  String  mid;
    private  String  cid;
    private  String  UserName;
    private  String  Password;

}
