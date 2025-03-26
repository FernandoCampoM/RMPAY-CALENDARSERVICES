package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;

public interface UsersAppRepository extends CrudRepository<UsersBusiness,Long>{
    
    List<UsersBusiness> findByBusiness(Business business);

    Optional<UsersBusiness> findByUsername(String username);
    @Query("SELECT u FROM UsersBusiness u WHERE u.password = :password AND u.business.businessId = :businessId")
    List<UsersBusiness> findByPasswordAndBusinessId(String password, Long businessId);
    List<UsersBusiness> findByPassword(String password);

    @Modifying
    @Query("UPDATE UsersBusiness u SET u.enable = :enable WHERE u.userBusinessId = :userBusinessId")
    void updateEnable(Long userBusinessId, boolean enable);

    @Modifying
    @Query("UPDATE UsersBusiness u SET u.download = :download WHERE u.userBusinessId != :userBusinessId")
    void updateAllDownloadExceptMe(Long userBusinessId, boolean download);


    @Query(value = """
    WITH HorasProgramadas AS (
        SELECT 
            s.userBusinessId,
            SUM(DATEDIFF(HOUR, CONCAT(s.startDate, ' ', s.startTime), CONCAT(s.endDate, ' ', s.endTime))) AS horas_programadas,
            SUM(DATEDIFF(HOUR, CONCAT(s.startDate, ' ', s.startTime), CONCAT(s.endDate, ' ', s.endTime)) * ub.costHour) AS costo_programado
        FROM Shift s
        JOIN UsersBusiness ub ON s.userBusinessId = ub.userBusinessId
        WHERE s.startDate >= :startDate AND s.endDate <= :endDate
        GROUP BY s.userBusinessId, ub.costHour
    ),
    HorasDiarias AS (
        SELECT 
            e.userBusinessId,
            e.date,
            DATEDIFF(HOUR, MIN(e.hour), MAX(e.hour)) AS horas_trabajadas_diarias,
            DATEDIFF(HOUR, MIN(e.hour), MAX(e.hour)) * ub.costHour AS costo_real_diario
        FROM EntryExit e
        JOIN UsersBusiness ub ON e.userBusinessId = ub.userBusinessId
        WHERE e.date BETWEEN :startDate AND :endDate
        GROUP BY e.userBusinessId, e.date, ub.costHour
    ),
    HorasTrabajadas AS (
        SELECT 
            userBusinessId,
            SUM(horas_trabajadas_diarias) AS horas_trabajadas,
            SUM(costo_real_diario) AS costo_real
        FROM HorasDiarias
        GROUP BY userBusinessId
    )
    SELECT 
        ub.username,
        COALESCE(hp.horas_programadas, 0) AS horas_programadas,
        COALESCE(ht.horas_trabajadas, 0) AS horas_trabajadas,
        COALESCE(ht.horas_trabajadas, 0) - COALESCE(hp.horas_programadas, 0) AS diferencia_horas,
        COALESCE(hp.costo_programado, 0) AS costo_programado,
        COALESCE(ht.costo_real, 0) AS costo_real,
        COALESCE(ht.costo_real, 0) - COALESCE(hp.costo_programado, 0) AS diferencia_costo
    FROM UsersBusiness ub
    LEFT JOIN HorasProgramadas hp ON ub.userBusinessId = hp.userBusinessId
    LEFT JOIN HorasTrabajadas ht ON ub.userBusinessId = ht.userBusinessId
    ORDER BY ub.username;
""", nativeQuery = true)
List<Object[]> reporteHorasTrabajadas(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

@Query(value = """
    WITH HorasProgramadas AS (
        SELECT 
            s.userBusinessId,
            SUM(DATEDIFF(HOUR, CONCAT(s.startDate, ' ', s.startTime), CONCAT(s.endDate, ' ', s.endTime))) AS horas_programadas,
            SUM(DATEDIFF(HOUR, CONCAT(s.startDate, ' ', s.startTime), CONCAT(s.endDate, ' ', s.endTime)) * ub.costHour) AS costo_programado
        FROM Shift s
        JOIN UsersBusiness ub ON s.userBusinessId = ub.userBusinessId
        WHERE s.startDate >= :startDate 
          AND s.endDate <= :endDate 
          AND ub.businessId = :businessId
        GROUP BY s.userBusinessId, ub.costHour
    ),
    HorasDiarias AS (
        SELECT 
            e.userBusinessId,
            e.date,
            DATEDIFF(HOUR, MIN(e.hour), MAX(e.hour)) AS horas_trabajadas_diarias,
            DATEDIFF(HOUR, MIN(e.hour), MAX(e.hour)) * ub.costHour AS costo_real_diario
        FROM EntryExit e
        JOIN UsersBusiness ub ON e.userBusinessId = ub.userBusinessId
        WHERE e.date BETWEEN :startDate AND :endDate 
          AND ub.businessId = :businessId
        GROUP BY e.userBusinessId, e.date, ub.costHour
    ),
    HorasTrabajadas AS (
        SELECT 
            userBusinessId,
            SUM(horas_trabajadas_diarias) AS horas_trabajadas,
            SUM(costo_real_diario) AS costo_real
        FROM HorasDiarias
        GROUP BY userBusinessId
    )
    SELECT 
        ub.username,
        COALESCE(hp.horas_programadas, 0) AS horas_programadas,
        COALESCE(ht.horas_trabajadas, 0) AS horas_trabajadas,
        COALESCE(ht.horas_trabajadas, 0) - COALESCE(hp.horas_programadas, 0) AS diferencia_horas,
        COALESCE(hp.costo_programado, 0) AS costo_programado,
        COALESCE(ht.costo_real, 0) AS costo_real,
        COALESCE(ht.costo_real, 0) - COALESCE(hp.costo_programado, 0) AS diferencia_costo
    FROM UsersBusiness ub
    LEFT JOIN HorasProgramadas hp ON ub.userBusinessId = hp.userBusinessId
    LEFT JOIN HorasTrabajadas ht ON ub.userBusinessId = ht.userBusinessId
    WHERE ub.businessId = :businessId
    ORDER BY ub.username;
""", nativeQuery = true)
    List<Object[]> reporteHorasTrabajadas(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("businessId") Long businessId
    );

}
