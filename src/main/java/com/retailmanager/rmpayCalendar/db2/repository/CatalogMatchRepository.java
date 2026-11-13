package com.retailmanager.rmpayCalendar.db2.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpayCalendar.db2.entity.CatalogMatch;
import com.retailmanager.rmpayCalendar.enums.MatchStatus;

public interface CatalogMatchRepository extends JpaRepository<CatalogMatch, Long> {

    CatalogMatch findByMatchKey(String matchKey);

    @Query("SELECT m.status, COUNT(m) FROM CatalogMatch m GROUP BY m.status")
    List<Object[]> countByStatus();

    /**
     * Búsqueda paginada para la pantalla de conciliación.
     * @param statuses estados a incluir (nunca vacío)
     * @param q texto en minúsculas con comodines, o null
     * @param hideDrafts true para excluir borradores de Shopify
     */
    @Query("SELECT m FROM CatalogMatch m WHERE m.status IN :statuses "
            + "AND (:q IS NULL OR LOWER(m.shopifyTitle) LIKE :q OR LOWER(m.rmName) LIKE :q "
            + "     OR LOWER(m.shopifySku) LIKE :q OR LOWER(m.shopifyBarcode) LIKE :q OR LOWER(m.rmProductCode) LIKE :q) "
            + "AND (:hideDrafts = false OR m.shopifyStatus IS NULL OR m.shopifyStatus <> 'DRAFT')")
    Page<CatalogMatch> search(@Param("statuses") List<MatchStatus> statuses,
                              @Param("q") String q,
                              @Param("hideDrafts") boolean hideDrafts,
                              Pageable pageable);

    List<CatalogMatch> findAllByStatusIn(List<MatchStatus> statuses);
}
