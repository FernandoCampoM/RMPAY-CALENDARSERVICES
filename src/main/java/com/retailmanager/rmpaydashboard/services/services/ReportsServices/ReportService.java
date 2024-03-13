package com.retailmanager.rmpaydashboard.services.services.ReportsServices;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.SaleRepository;

import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.DailySummaryDTO;

@Service
public class ReportService implements IReportService {
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private SaleRepository serviceDBSale;

    /**
     * Retrieves the daily summary for a given business ID.
     *
     * @param  businessId   the ID of the business
     * @return              a ResponseEntity containing the daily summary data
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getDailySummary(Long businessId) {
        DailySummaryDTO dailySummaryDTO=new DailySummaryDTO();
        Object [] dailySummary=this.serviceDBSale.dailySummary(businessId);
        Object [] dailySummaryV=(Object[]) dailySummary[0];
        if(dailySummaryV[0]!=null){
            dailySummaryDTO.setTotalSales(Double.parseDouble(dailySummaryV[0].toString()));
        }
        if(dailySummaryV[1]!=null){
            dailySummaryDTO.setTotalRefunds(Double.parseDouble(dailySummaryV[1].toString()));
        }
        if(dailySummaryV[2]!=null){
            dailySummaryDTO.setStateTax(Double.parseDouble(dailySummaryV[2].toString()));
        }
        if(dailySummaryV[3]!=null){
            dailySummaryDTO.setMunicipalTax(Double.parseDouble(dailySummaryV[3].toString()));
        }
        if(dailySummaryV[4]!=null){
            dailySummaryDTO.setEstimatedRedTax(Double.parseDouble(dailySummaryV[4].toString()));
        }
        //TODO: FALTA BENEFIT Y PROPINAS
        //TODO: consultar a la base de datos el reporte de sales por categorias
        HashMap<String,String> salesByCategory=new HashMap<>();
        salesByCategory.put("category", "Categoria Prueba");
        salesByCategory.put("totalAmount", "1000");
        dailySummaryDTO.getSalesByCategory().add(salesByCategory);
        //TODO: consultar a la base de datos el reporte de ventas de los productos mas vendidos
        HashMap<String,String> bestSellingProducts=new HashMap<>();
        bestSellingProducts.put("name", "producto Prueba");
        bestSellingProducts.put("quantity", "10");
        bestSellingProducts.put("totalAmount", "1000");
        bestSellingProducts.put("benefit", "1000");
        dailySummaryDTO.getBestSellingProducts().add(bestSellingProducts);
        return new ResponseEntity<>(dailySummaryDTO,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getSummaryByDateRangee(Long businessId, LocalDate startDate, LocalDate endDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSummaryByDateRangee'");
    }

    @Override
    public ResponseEntity<?> getLowInventory(Long businessId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLowInventory'");
    }

    @Override
    public ResponseEntity<?> getBestSellingItems(Long businessId, LocalDate startDate, LocalDate endDate,
            String categoria) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBestSellingItems'");
    }

    @Override
    public ResponseEntity<?> getSalesByCategory(Long businessId, LocalDate startDate, LocalDate endDate,
            String categoria) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSalesByCategory'");
    }

    @Override
    public ResponseEntity<?> getEarningsReport(Long businessId, LocalDate startDate, LocalDate endDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEarningsReport'");
    }

    @Override
    public ResponseEntity<?> getTips(Long businessId, LocalDate startDate, LocalDate endDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTips'");
    }

    @Override
    public ResponseEntity<?> getTaxes(Long businessId, LocalDate startDate, LocalDate endDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTaxes'");
    }
    
}
