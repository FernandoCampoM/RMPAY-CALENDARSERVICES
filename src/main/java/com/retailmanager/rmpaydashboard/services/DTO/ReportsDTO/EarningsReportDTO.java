package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EarningsReportDTO {
    private double totalSales=0;
    private double subTotalSales = 0;
    private double benefit = 0;
    private double stateTax = 0;
    private double estimatedRedTax = 0;
    private double municipalTax = 0;
    List<HashMap<String,String>> earningsByCategory= new ArrayList<>();
    List<HashMap<String,String>> bestSellingProducts= new ArrayList<>();
    
}
