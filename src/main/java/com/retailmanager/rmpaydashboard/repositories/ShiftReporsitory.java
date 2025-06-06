package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.retailmanager.rmpaydashboard.models.Shift;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;

public interface ShiftReporsitory extends CrudRepository<Shift, Long>,  PagingAndSortingRepository<Shift, Long> {
    @Query("select s from Shift s where s.userBusiness.userBusinessId = :userBusinessId and s.terminal.terminalId = :terminalId")
    public Shift findByEmployeeAndTerminal(Long userBusinessId, String terminalId);

    public Optional<Shift> findFirstByUserBusinessAndTerminal(UsersBusiness Employee, Terminal terminal);

    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific UsersBusiness 
     * and Terminal, and have a start time within the specified date and time range.
     *
     * @param userBusiness the UsersBusiness entity to filter by.
     * @param terminal the Terminal entity to filter by.
     * @param startTime the start of the date-time range to filter shifts.
     * @param endTime the end of the date-time range to filter shifts.
     * @param pageable the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */

    public Page<Shift> findByUserBusinessAndTerminalAndStartTimeBetween(
        UsersBusiness userBusiness, 
        Terminal terminal, 
        LocalDateTime startTime, 
        LocalDateTime endTime, 
        Pageable pageable
    );
    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific 
     * UsersBusiness and Terminal.
     *
     * @param userBusiness the UsersBusiness entity to filter by.
     * @param terminal the Terminal entity to filter by.
     * @param pageable the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */

    public Page<Shift> findByUserBusinessAndTerminal(
        UsersBusiness userBusiness, Terminal terminal, Pageable pageable);

    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific 
     * UsersBusiness and have a start time within the specified date and time range.
     *
     * @param userBusiness the UsersBusiness entity to filter by.
     * @param startTime the start of the date-time range to filter shifts.
     * @param endTime the end of the date-time range to filter shifts.
     * @param pageable the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */

    public Page<Shift> findByUserBusinessAndStartTimeBetween(
        UsersBusiness userBusiness, 
        LocalDateTime startTime, 
        LocalDateTime endTime, 
        Pageable pageable
    );
    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific 
     * Terminal and have a start time within the specified date and time range.
     *
     * @param terminal the Terminal entity to filter by.
     * @param startTime the start of the date-time range to filter shifts.
     * @param endTime the end of the date-time range to filter shifts.
     * @param pageable the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */
    public Page<Shift> findByTerminalAndStartTimeBetween(
        Terminal terminal, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific 
     * UsersBusiness.
     *
     * @param userBusiness the UsersBusiness entity to filter by.
     * @param pageable the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */
    public Page<Shift> findByUserBusiness(
        UsersBusiness userBusiness, Pageable pageable);
    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific 
     * Terminal.
     *
     * @param terminal the Terminal entity to filter by.
     * @param pageable the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */
    public Page<Shift> findByTerminal(
        Terminal terminal, Pageable pageable);

    /**
     * Retrieves a paginated list of Shift entities that have a start time within the 
     * specified date and time range.
     *
     * @param startTime the start of the date-time range to filter shifts.
     * @param endTime the end of the date-time range to filter shifts.
     * @param pageable the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */
    public Page<Shift>    findByStartTimeBetween(
        LocalDateTime startTime, 
        LocalDateTime endTime, 
        Pageable pageable
    );
    Page<Shift> findByUserBusinessAndTerminalAndStartTimeBetweenAndStatusShiftBalance(UsersBusiness userBusiness, Terminal terminal, LocalDateTime start, LocalDateTime end, Boolean statusShiftBalance, Pageable pageable);
    Page<Shift> findByUserBusinessAndTerminalAndStatusShiftBalance(UsersBusiness userBusiness, Terminal terminal, Boolean statusShiftBalance, Pageable pageable);
    Page<Shift> findByUserBusinessAndStartTimeBetweenAndStatusShiftBalance(UsersBusiness userBusiness, LocalDateTime start, LocalDateTime end, Boolean statusShiftBalance, Pageable pageable);
    Page<Shift> findByTerminalAndStartTimeBetweenAndStatusShiftBalance(Terminal terminal, LocalDateTime start, LocalDateTime end, Boolean statusShiftBalance, Pageable pageable);
    Page<Shift> findByUserBusinessAndStatusShiftBalance(UsersBusiness userBusiness, Boolean statusShiftBalance, Pageable pageable);
    Page<Shift> findByTerminalAndStatusShiftBalance(Terminal terminal, Boolean statusShiftBalance, Pageable pageable);
    Page<Shift> findByStartTimeBetweenAndStatusShiftBalance(LocalDateTime start, LocalDateTime end, Boolean statusShiftBalance, Pageable pageable);
    Page<Shift> findByStatusShiftBalance(Boolean statusShiftBalance, Pageable pageable); // Nuevo caso base
}   
