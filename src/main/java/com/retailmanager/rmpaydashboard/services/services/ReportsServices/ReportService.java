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

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.ItemForSale;
import com.retailmanager.rmpaydashboard.models.Sale;
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
        Object [] dailySummaryV=null;
        if(dailySummary!=null && dailySummary[0]!=null){
            dailySummaryV=(Object[]) dailySummary[0];
        }else{
            return new ResponseEntity<String>("{\"message\":\"No existen ventas para el Business con businessId "+businessId+"\"}",HttpStatus.NOT_FOUND);
        }
        
        Business business=serviceDBBusiness.findById(businessId).orElse(null);
        if(business==null){
            throw new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
        }

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
        List<Sale> sales =this.serviceDBSale.findBySaleTransactionTypeAndSaleStatusAndBusinessAndSaleEndDate("SALE", "SUCCEED", business, LocalDate.now());
        Double benefit=0.0;
        Double propinas=0.0;
        if(sales!=null && sales.size()>0){
            for(Sale sale:sales){
                for(ItemForSale item:sale.getItemsList()){
                    benefit+=item.getGrossProfit();
                }
                propinas+=sale.getTipAmount();
            }
        }
        dailySummaryDTO.setBenefit(benefit);
        dailySummaryDTO.setTotalTips(propinas);
        
        Object [] dailySummaryByCategory=this.serviceDBSale.dailySummaryForCategory(businessId);
        if(dailySummaryByCategory!=null){
            for(int i=0;i<dailySummaryByCategory.length;i++){
                Object [] dailySummaryByCategoryV=(Object[]) dailySummaryByCategory[i];
                HashMap<String,String> salesByCategory=new HashMap<>();
                salesByCategory.put("category", objectToString(dailySummaryByCategoryV[0]));
                salesByCategory.put("totalAmount", objectToString(dailySummaryByCategoryV[1]));
                dailySummaryDTO.getSalesByCategory().add(salesByCategory);
            }
            
        }
        Object [] dailySummaryBestSellingItems=this.serviceDBSale.dailySummaryBestSellingItems(businessId);
        if(dailySummaryBestSellingItems!=null){
            for(int i=0;i<dailySummaryBestSellingItems.length;i++){
                Object [] dailySummaryBestSellingItemsV=(Object[]) dailySummaryBestSellingItems[i];
                HashMap<String,String> bestSellingProducts=new HashMap<>();
                bestSellingProducts.put("name", objectToString(dailySummaryBestSellingItemsV[4]));
                bestSellingProducts.put("quantity", objectToString(dailySummaryBestSellingItemsV[1]));
                bestSellingProducts.put("totalAmount", objectToString(dailySummaryBestSellingItemsV[2]));
                bestSellingProducts.put("benefit", objectToString(dailySummaryBestSellingItemsV[3]));
                dailySummaryDTO.getBestSellingProducts().add(bestSellingProducts);
            }
            
        }
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
    private String objectToString(Object obj){
        return obj != null ? obj.toString() : "";
    }
}
