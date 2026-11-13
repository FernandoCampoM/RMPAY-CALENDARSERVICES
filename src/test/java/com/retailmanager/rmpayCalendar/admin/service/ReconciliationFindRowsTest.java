package com.retailmanager.rmpayCalendar.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.retailmanager.rmpayCalendar.db2.entity.CatalogMatch;
import com.retailmanager.rmpayCalendar.db2.repository.CatalogMatchRepository;

/**
 * Aplicar acciones con miles de filas (por ejemplo crear en RM 2535 productos de Shopify): las
 * filas se cargan en lotes porque SQL Server admite hasta 2100 parámetros por consulta.
 */
class ReconciliationFindRowsTest {

    @Test
    @SuppressWarnings("unchecked")
    void cargaMilesDeFilasEnLotesDeMenosDe2100() {
        CatalogMatchRepository repo = mock(CatalogMatchRepository.class);
        when(repo.findAllById(any())).thenAnswer(i -> {
            List<CatalogMatch> rows = new ArrayList<>();
            for (Long id : (Iterable<Long>) i.getArgument(0)) {
                CatalogMatch m = new CatalogMatch();
                m.setId(id);
                rows.add(m);
            }
            return rows;
        });
        ReconciliationService service = new ReconciliationService();
        ReflectionTestUtils.setField(service, "matchRepository", repo);
        List<Long> ids = LongStream.rangeClosed(1, 2535).boxed().collect(Collectors.toList());

        List<CatalogMatch> rows = service.findRows(ids);

        assertEquals(2535, rows.size());
        assertEquals(ids, rows.stream().map(CatalogMatch::getId).collect(Collectors.toList()));
        ArgumentCaptor<Iterable<Long>> batches = ArgumentCaptor.forClass(Iterable.class);
        verify(repo, atLeastOnce()).findAllById(batches.capture());
        assertEquals(3, batches.getAllValues().size());
        batches.getAllValues().forEach(b -> {
            int n = 0;
            for (Long ignored : b) {
                n++;
            }
            assertTrue(n <= ReconciliationService.ROWS_PER_QUERY && n < 2100);
        });
    }
}
