
package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter 
public class TokenPayment extends RequestPayment{
    private String token;
    private  String Amount;
    private  String ZipCode;
    private  String ExpDate;
    private  String NameOnCard;
    private  String CVN;
    private  String Track2;
    private  String UserTransactionNumber;
    private  String TransactionType;
    
    private  String HostUserName;
    private  String HostUserPassword;
    private  String reversalid;

    public TokenPayment(String  AppKey, String  AppType, String  mid, String  cid, String  UserName, String  Password, 
            String Amount, String ZipCode, String token, String ExpDate, String NameOnCard, 
            String CVN, String Track2, String UserTransactionNumber, String TransactionType) {
        super(AppKey, AppType, mid, cid, UserName, Password);
        this.Amount = Amount;
        this.ZipCode = ZipCode;
        this.token = token;
        this.ExpDate = ExpDate;
        this.NameOnCard = NameOnCard;
        this.CVN = CVN;
        this.Track2 = Track2;
        this.UserTransactionNumber = UserTransactionNumber;
        this.TransactionType = TransactionType;
    }

    public TokenPayment(String AppKey, String AppType, String mid, String cid, String UserName, String Password, String Amount, String ZipCode, String token, String ExpDate, String NameOnCard, String CVN, String Track2, String UserTransactionNumber, String TransactionType, String HostUserName, String HostUserPassword, String reversalid) {
        super(AppKey, AppType, mid, cid, UserName, Password);
        this.Amount = Amount;
        this.ZipCode = ZipCode;
        this.token = token;
        this.ExpDate = ExpDate;
        this.NameOnCard = NameOnCard;
        this.CVN = CVN;
        this.Track2 = Track2;
        this.UserTransactionNumber = UserTransactionNumber;
        this.TransactionType = TransactionType;
        this.HostUserName = HostUserName;
        this.HostUserPassword = HostUserPassword;
        this.reversalid = reversalid;
    }
}
