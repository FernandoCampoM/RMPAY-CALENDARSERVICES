package com.retailmanager.rmpaydashboard.security;

import lombok.Builder;
import lombok.Data;

@Data

public class AuthCredentials {
    private String username;
    private String password;

}
