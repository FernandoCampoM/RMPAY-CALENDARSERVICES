package com.retailmanager.rmpaydashboard.services.services.ReportsServices;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

public interface IReportService {

    public ResponseEntity<?> getDailySummary(Long businessId, LocalDate date);
    public ResponseEntity<?> getSummaryByDateRangee(Long businessId, LocalDate startDate, LocalDate endDate);
    public ResponseEntity<?> getLowInventory(Long businessId);
    public ResponseEntity<?> getBestSellingItems(Long businessId,LocalDate startDate, LocalDate endDate, String categoria);
    public ResponseEntity<?> getSalesByCategory(Long businessId,LocalDate startDate, LocalDate endDate);
    public ResponseEntity<?> getEarningsReport(Long businessId,LocalDate startDate, LocalDate endDate);
    /**
     * Get tips(PROPINAS) by business and date range
     * @param businessId
     * @param startDate
     * @param endDate
     * @return
     */
    public ResponseEntity<?> getTips(Long businessId,LocalDate startDate, LocalDate endDate);
    public ResponseEntity<?> getTaxes(Long businessId,LocalDate startDate, LocalDate endDate);

    public ResponseEntity<?> getReceipts(Long businessId,LocalDate startDate, LocalDate endDate);
}
