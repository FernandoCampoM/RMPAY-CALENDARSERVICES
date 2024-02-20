package com.retailmanager.rmpaydashboard.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Terminal;
import java.util.List;


public interface TerminalRepository extends CrudRepository<Terminal, Long> {
    Optional<Terminal> findOneBySerial(String serial);
    
    List<Terminal> findByBusiness(Business business);
    @Modifying
    @Query("UPDATE Terminal u SET u.enable = :enable WHERE u.terminalId = :terminalId")
    void updateEnable(Long terminalId, boolean enable);
}
