
package com.retailmanager.rmpaydashboard.utils;

import com.google.gson.Gson;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponseJSON;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponsePayment;

/**
 *
 * @author pomeo
 */
public class ConverterJson {
    public static ResponsePayment convertStr2RespPay(String json){
        return new Gson().fromJson(json, ResponsePayment.class);
    }
    public static ResponseJSON convertStr2RespJson(String json){
        return new Gson().fromJson(json, ResponseJSON.class);
    }
    
}
