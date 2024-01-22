package com.retailmanager.rmpaydashboard.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Utils {
    public static void main(String[] args){
        String texto="rmpayadmin";

        String textoCodificado=new BCryptPasswordEncoder().encode(texto);
        System.out.println(textoCodificado);
        System.out.println(new BCryptPasswordEncoder().matches("developer@601", "$2a$10$Bjs3O7PETpSRNt2KAXQIjeSI5kCa/NTG7XLE.MvmVWaOXvPrj2Wbm"));
    }
}
