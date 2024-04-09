package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TipsReportDTO {
    private double totalSales=0;
    private double subTotalSales = 0;
    private double totalTips = 0;
    List<HashMap<String,String>> userTips= new ArrayList<>();
}
