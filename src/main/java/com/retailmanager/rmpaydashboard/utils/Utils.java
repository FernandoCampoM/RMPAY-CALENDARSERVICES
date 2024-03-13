package com.retailmanager.rmpaydashboard.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Utils {
    public static void main(String[] args){
        String texto="ejemploUsuario";

        String textoCodificado=new BCryptPasswordEncoder().encode(texto);
        System.out.println(textoCodificado);
        System.out.println(new BCryptPasswordEncoder().matches("ejemploUsuario", "$2a$10$MrqOwjIdvn4JMjYMbGEt/exnNlQEf7ytzMU6Jr1Jv01YR.uhrK7BO"));
        
        
    
    }
}
