package com.retailmanager.rmpaydashboard.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.services.ReportsServices.IReportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Validated
public class ReportsController {
    @Autowired
    private IReportService reportService;

    @GetMapping("/reports/daily-summary")
    public ResponseEntity<?> getDailySummary(@RequestParam(name = "businessId") Long businessId) {
        return reportService.getDailySummary(businessId);
    }

    @GetMapping("/reports/summary-by-date-range")
    public ResponseEntity<?> getSummaryByDateRange(@RequestParam(name = "businessId") Long businessId,
                                                    @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                                    @RequestParam(name = "endDate") @Valid LocalDate endDate) {
        return reportService.getSummaryByDateRangee(businessId, startDate, endDate);
    }

    @GetMapping("/reports/low-inventory")
    public ResponseEntity<?> getLowInventory(@RequestParam(name = "businessId") Long businessId) {
        return reportService.getLowInventory(businessId);
    }

    @GetMapping("/reports/best-selling-items")
    public ResponseEntity<?> getBestSellingItems(@RequestParam(name = "businessId") Long businessId,
                                                  @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                                  @RequestParam(name = "endDate") @Valid LocalDate endDate,
                                                  @RequestParam(name = "categoria") String categoria) {
        return reportService.getBestSellingItems(businessId, startDate, endDate, categoria);
    }

    @GetMapping("/reports/sales-by-category")
    public ResponseEntity<?> getSalesByCategory(@RequestParam(name = "businessId") Long businessId,
                                                 @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                                 @RequestParam(name = "endDate") @Valid LocalDate endDate,
                                                 @RequestParam(name = "categoria") String categoria) {
        return reportService.getSalesByCategory(businessId, startDate, endDate, categoria);
    }

    @GetMapping("/reports/earnings-report")
    public ResponseEntity<?> getEarningsReport(@RequestParam(name = "businessId") Long businessId,
                                                @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                                @RequestParam(name = "endDate") @Valid LocalDate endDate) {
        return reportService.getEarningsReport(businessId, startDate, endDate);
    }

    @GetMapping("/reports/tips")
    public ResponseEntity<?> getTips(@RequestParam(name = "businessId") Long businessId,
                                      @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                      @RequestParam(name = "endDate") @Valid LocalDate endDate) {
        return reportService.getTips(businessId, startDate, endDate);
    }

    @GetMapping("/reports/taxes")
    public ResponseEntity<?> getTaxes(@RequestParam(name = "businessId") Long businessId,
                                       @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                       @RequestParam(name = "endDate") @Valid LocalDate endDate) {
        return reportService.getTaxes(businessId, startDate, endDate);
    }
}
