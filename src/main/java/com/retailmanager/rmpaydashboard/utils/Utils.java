package com.retailmanager.rmpaydashboard.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Utils {
    public static void main(String[] args){
        String texto="ejemploUsuario";

        String textoCodificado=new BCryptPasswordEncoder().encode(texto);
        System.out.println(textoCodificado);
        System.out.println(new BCryptPasswordEncoder().matches("ejemploUsuario", "$2a$10$3hXD2CTLL18GTNFCZiYfWuWyhMQFc30EvsMh5fWnCIXKPfflhe/mC"));
        
        
        String originalFileName = "archivo con caracteres : no permitidos.txt";
        String safeFileName = generateSafeFileName(originalFileName);
        System.out.println("Nombre seguro para el archivo: " + safeFileName);
    
    }
    public static String generateSafeFileName(String originalFileName) {
        // Normalizar el nombre de archivo para eliminar caracteres especiales o no ASCII
        String normalizedFileName = Normalizer.normalize(originalFileName, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        
        // Eliminar caracteres no permitidos en nombres de archivos
        normalizedFileName = normalizedFileName.replaceAll("[/\\\\:*?\"<>| ]", "_");
        
        // Limitar la longitud del nombre de archivo si es necesario
        int maxLength = 255; // Longitud máxima permitida por la mayoría de los sistemas de archivos
        if (normalizedFileName.length() > maxLength) {
            normalizedFileName = normalizedFileName.substring(0, maxLength);
        }
        
        return normalizedFileName;
    }

}
