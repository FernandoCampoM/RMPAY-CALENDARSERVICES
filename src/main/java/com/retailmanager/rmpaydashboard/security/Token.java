package com.retailmanager.rmpaydashboard.security;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Token {
    String Authorization;
    public String toJSON(){
        return "{\"Authorization\":\""+Authorization+"\"}";
    }
}
