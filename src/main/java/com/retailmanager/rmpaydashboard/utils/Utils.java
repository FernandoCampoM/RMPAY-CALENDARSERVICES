package com.retailmanager.rmpaydashboard.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.util.Random;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Utils {
    public static void main(String[] args){
        String texto="rmpayuser";
        String textoCodificado=new BCryptPasswordEncoder().encode(texto);
        System.out.println(textoCodificado);
        System.out.println(new BCryptPasswordEncoder().matches("rmpayuser", "$2a$10$3hXD2CTLL18GTNFCZiYfWuWyhMQFc30EvsMh5fWnCIXKPfflhe/mC"));
        
        
        String originalFileName = "archivo con caracteres : no permitidos.txt";
        String safeFileName = generateSafeFileName(originalFileName);
        System.out.println("Nombre seguro para el archivo: " + safeFileName);

        try {
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("IP address of my PC: " + ip.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("ID unico: " + generateUniqueId());
        System.out.println("ID unico: " + generateUniqueId());
        System.out.println("ID unico: " + generateUniqueId());
    }
    public static long generateUniqueId() {
         Random random = new Random();
        long currentTimeMillis = System.currentTimeMillis();
        int randomInt = random.nextInt(1000); // Agrega una aleatoriedad para reducir colisiones
        return currentTimeMillis + randomInt; // Combina el tiempo y el número aleatorio
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
