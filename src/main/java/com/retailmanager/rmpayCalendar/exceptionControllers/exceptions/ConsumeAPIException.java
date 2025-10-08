package com.retailmanager.rmpayCalendar.exceptionControllers.exceptions;

public class ConsumeAPIException extends Exception{
    private Integer httpStatusCode;
    
    public ConsumeAPIException(String msg, Integer httpsStatusCode){
        super(msg);
        this.httpStatusCode = httpsStatusCode;
    }
    
    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }
 
    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}
