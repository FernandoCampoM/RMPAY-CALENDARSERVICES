package com.retailmanager.rmpayCalendar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class RmpaydashboardApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context=SpringApplication.run(RmpaydashboardApplication.class, args);
		createIfNoExistsFolder();

	}
	private static void createIfNoExistsFolder() {
		String rutaProyecto = System.getProperty("user.dir");
        Path nuevaCarpeta = Paths.get(rutaProyecto, "images");

        try {
            // Crear la carpeta si no existe
            if (!Files.exists(nuevaCarpeta)) {
                Files.createDirectory(nuevaCarpeta);
                System.out.println("Carpeta creada en: " + nuevaCarpeta.toAbsolutePath());
            } else {
                System.out.println("La carpeta ya existe en: " + nuevaCarpeta.toAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
