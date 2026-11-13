package com.retailmanager.rmpayCalendar.backgroundRoutines;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import com.retailmanager.rmpayCalendar.services.services.Shopify.InvoiceSyncJob;
import com.retailmanager.rmpayCalendar.services.services.Shopify.ProductSyncJob;

/**
 * Productos y pedidos corren por separado: aunque el ciclo de productos quede ocupado
 * (por ejemplo creando miles de productos en Shopify), el de pedidos se ejecuta igual.
 * La rutina arranca en el minuto siguiente, así que el test puede tardar hasta un minuto.
 */
class ShopifyRoutinesTest {

    @Test
    void pedidosNoEsperanAQueTermineElCicloDeProductos() throws Exception {
        CountDownLatch productStarted = new CountDownLatch(1);
        CountDownLatch releaseProducts = new CountDownLatch(1);
        CountDownLatch invoicesRan = new CountDownLatch(1);

        ProductSyncJob productJob = mock(ProductSyncJob.class);
        doAnswer(i -> {
            productStarted.countDown();
            releaseProducts.await(); // ciclo de productos "trabado"
            return null;
        }).when(productJob).execute();
        InvoiceSyncJob invoiceJob = mock(InvoiceSyncJob.class);
        doAnswer(i -> {
            invoicesRan.countDown();
            return null;
        }).when(invoiceJob).execute();

        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        when(context.getBean(ProductSyncJob.class)).thenReturn(productJob);
        when(context.getBean(InvoiceSyncJob.class)).thenReturn(invoiceJob);

        ShopifyRoutines routines = new ShopifyRoutines(context);
        routines.setDaemon(true);
        routines.start();
        try {
            assertTrue(productStarted.await(90, TimeUnit.SECONDS), "el ciclo de productos debe arrancar");
            assertTrue(invoicesRan.await(30, TimeUnit.SECONDS),
                    "el ciclo de pedidos debe correr aunque productos siga ocupado");
        } finally {
            releaseProducts.countDown();
        }
    }
}
