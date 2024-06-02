package com.retailmanager.rmpaydashboard.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Terminal;

import java.time.LocalDate;
import java.util.List;


public interface TerminalRepository extends CrudRepository<Terminal, Long> {
    Optional<Terminal> findOneBySerial(String serial);
    
    List<Terminal> findByBusiness(Business business);
    @Modifying
    @Query("UPDATE Terminal u SET u.enable = :enable WHERE u.terminalId = :terminalId")
    void updateEnable(Long terminalId, boolean enable);
    /**
     * Obtiene los terminles expirados de un negocio
     *
     * @param  business     negocio
     * @param  date         fecha para comparar con la fecha de expiración
     * @return              description of return value
     */
    //
    public List<Terminal> findByBusinessAndExpirationDateLessThan(Business business, LocalDate date);

    @Query("SELECT b FROM Terminal b WHERE MONTH(b.lastPayment) = :month AND YEAR(b.lastPayment) = :year")
    List<Terminal> findAllByRegisterMonthAndYear(int month,  int year);
    
}
