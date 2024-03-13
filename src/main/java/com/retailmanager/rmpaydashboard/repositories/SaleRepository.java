package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Sale;

public interface SaleRepository extends CrudRepository<Sale, Long>  {
    
    public List<Sale> findByMerchantId(String merchantId);
    public List<Sale> findBySaleTransactionTypeAndSaleStatusAndMerchantId(String saleTransactionType, String saleStatus, String merchantId);
    public List<Sale> findBySaleTransactionTypeAndMerchantId(String saleTransactionType, String merchantId);

    public List<Sale> findBySaleEndDateBetweenAndSaleTransactionTypeAndSaleStatusAndMerchantId(LocalDate startDate, LocalDate endDate, String saleTransactionType, String saleStatus, String merchantId);


    ///FUNCIONES DE REPORTES
    
    @Query(value=" SELECT \r\n" + 
                "  (SELECT sum(saleTotalAmount) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId and saleEndDate='2022-02-20') as totalSales,\r\n" + 
                "  \r\n" + 
                "  (SELECT sum(saleToRefund) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId and saleEndDate='2022-02-20')as totalRefund,\r\n" + 
                "\r\n" + 
                "  (SELECT sum(saleStateTaxAmount)\r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId and saleEndDate='2022-02-20') as stateTax,\r\n" + 
                "\r\n" + 
                "  (SELECT sum(saleCityTaxAmount) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId and saleEndDate='2022-02-20')  as cityTax,\r\n" + 
                "\r\n" + 
                "  (SELECT sum(saleReduceTax) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId and saleEndDate='2022-02-20' ) as redTax", nativeQuery = true)
    public Object[] dailySummary(Long businessId);
}
