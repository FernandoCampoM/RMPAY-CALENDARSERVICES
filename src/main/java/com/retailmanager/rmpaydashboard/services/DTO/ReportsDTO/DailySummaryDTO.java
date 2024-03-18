package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DailySummaryDTO {
    private double totalSales=0;
    private double totalRefunds = 0;
    private double benefit = 0;
    private double totalTips = 0;
    private double stateTax = 0;
    private double estimatedRedTax = 0;
    private double municipalTax = 0;
    //retorna una lista de objetos category, totalAmount
    private List<HashMap<String,String>> salesByCategory= new ArrayList<>();
    //retorna una lista de objetos name, quantity, totalAmount, benefit
    private List<HashMap<String,String>> bestSellingProducts= new ArrayList<>();
    private List<HashMap<String,String>> bestSellingPayMethods= new ArrayList<>();
    private List<HashMap<String,String>> refundsSummay= new ArrayList<>();
    
}
