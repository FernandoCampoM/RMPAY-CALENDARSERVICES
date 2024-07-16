package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.retailmanager.rmpaydashboard.models.EntryExit;

public interface EntryExitRepository extends CrudRepository<EntryExit, Long>,  PagingAndSortingRepository<EntryExit, Long> {
    /**
     * oBTIENE UNA LISTA DE ENTRADAS Y SALIDAS POR UN EMPLEADO ORDENADAS POR FECHA Y HORA
     * @param userBusinessId
     * @return
     */
    @Query("SELECT e FROM EntryExit e WHERE e.userBusiness.userBusinessId = :userBusinessId ORDER BY e.date DESC, e.hour DESC")
    public List<EntryExit> getLastActivity(Long userBusinessId, Pageable pageable);

    @Query("SELECT e FROM EntryExit e WHERE e.userBusiness.business.businessId = :businessId AND e.date >= :startDate AND e.date <= :endDate ORDER BY e.date , e.hour ")
    public List<EntryExit> findByUserBusinessIdAndDate(Long businessId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT e FROM EntryExit e WHERE e.userBusiness.business.businessId = :businessId AND e.date >= :startDate AND e.date <= :endDate AND e.userBusiness.userBusinessId =:filter ORDER BY e.date , e.hour ")
    public List<EntryExit> findByUserBusinessIdAndDate(Long businessId, LocalDate startDate, LocalDate endDate, Long filter);
}
