package com.retailmanager.rmpayCalendar.security;

import lombok.Data;

@Data

public class AuthCredentials {
    private String username;
    private String password;

    private String terminalId;
}
