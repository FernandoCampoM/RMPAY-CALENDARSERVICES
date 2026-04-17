package com.retailmanager.rmpayCalendar.backgroundRoutines;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.context.ConfigurableApplicationContext;

import com.retailmanager.rmpayCalendar.services.services.Shopify.InvoiceSyncJob;
import com.retailmanager.rmpayCalendar.services.services.Shopify.ProductSyncJob;

public class ShopifyRoutines extends Thread {
    ProductSyncJob productSyncJob;
    InvoiceSyncJob invoiceSyncJob;
    public ShopifyRoutines(ConfigurableApplicationContext context){
        productSyncJob=context.getBean(ProductSyncJob.class);
        invoiceSyncJob=context.getBean(InvoiceSyncJob.class);
    }
    public void run(){
        System.out.println("<<<<<<RUTINA DE SINCORNIZACION DE SHOPIFY COMENZANDO...>>>>>>>>");
       //Obtener fecha y hora actual
         Calendar now = Calendar.getInstance();
         // Establece la hora y el minuto en que deseas que se dispare el evento
        int hour = 1;
        hour = now.get(Calendar.HOUR_OF_DAY);   // Hora en formato de 24 horas
        int minute = 0; // Minuto
        minute = now.get(Calendar.MINUTE)+1;
        System.out.println(">>>>>>>>>>>>>><<<<<<<<<<<La hora programada es: " + hour + ":" + minute);
        // Calcula la próxima fecha en que se debe disparar el evento
        Calendar nextExecutionTime = Calendar.getInstance();
        nextExecutionTime.set(Calendar.HOUR_OF_DAY, hour);
        nextExecutionTime.set(Calendar.MINUTE, minute);
        if (nextExecutionTime.before(now) || nextExecutionTime.equals(now)) {
            // Si la hora programada ya ha pasado hoy, suma un día para la próxima ejecución
            nextExecutionTime.add(Calendar.DATE, 1);
        }

        // Calcula la diferencia en milisegundos entre la próxima ejecución y la hora actual
        long initialDelay = nextExecutionTime.getTimeInMillis() - now.getTimeInMillis();

        // Crea un objeto TimerTask para ejecutar tu evento
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Coloca aquí el código que deseas que se ejecute en el evento diario
                System.out.println("🔥 RUTINA DE SINCORNIZACION DE SHOPIFY EJECUTANDOSE...");
                productSyncJob.execute();
                invoiceSyncJob.execute();
            }
        };

        // Crea un Timer y programa el TimerTask para que se ejecute todos los días a la misma hora
        Timer timer = new Timer();
        timer.schedule(task, initialDelay, 5 * 60 * 1000); // 24 horas en milisegundos

        // Espera indefinidamente para que el programa no finalice inmediatamente
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
