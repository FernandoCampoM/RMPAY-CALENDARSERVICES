package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Sale;

public interface SaleRepository extends CrudRepository<Sale, Long>  {
    /**
     * Obtiene una lista de ventas por identificador de comercio.
     *
     * @param  merchantId	identificador de comercio
     * @return         	listado de ventas
     */
    //
    public List<Sale> findByMerchantId(String merchantId);
    /**
     * Obtiene una lista de ventas por tipo de transacción, estado y identificador de comercio.
     *
     * @param  saleTransactionType	tipo de transacción
     * @param  saleStatus			estado
     * @param  merchantId			identificador de comercio
     * @return         			listado de ventas
     */
    //
    public List<Sale> findBySaleTransactionTypeAndSaleStatusAndMerchantId(String saleTransactionType, String saleStatus, String merchantId);
    /**
     * Obtiene una lista de ventas por tipo de transacción y identificador de comercio.
     *
     * @param  saleTransactionType  tipo de transacción
     * @param  merchantId           identificador de comercio
     * @return                      listado de ventas
     */
    //
    public List<Sale> findBySaleTransactionTypeAndMerchantId(String saleTransactionType, String merchantId);
    /**
     * Obtiene las ventas entre dos fechas y por tipo de transacción y estado e identificador de comercio.
     *
     * @param  startDate          fecha de inicio
     * @param  endDate            fecha de fin
     * @param  saleTransactionType tipo de transacción
     * @param  saleStatus         estado
     * @param  merchantId         identificador de comercio
     * @return                    listado de ventas
     */
    //
    public List<Sale> findBySaleEndDateBetweenAndSaleTransactionTypeAndSaleStatusAndMerchantId(LocalDate startDate, LocalDate endDate, String saleTransactionType, String saleStatus, String merchantId);


    ///FUNCIONES DE REPORTES
    //TODO: MODIFICAR EL CALCULO DE REFUND DE ACUERDO AL TIPO DE TRANSACCION
    /**
     * Retrieves daily summary data for a specific business.
     *
     * @param  businessId   the ID of the business
     * @return              an array containing total sales, total refund, state tax, city tax, and reduced tax
     */
    @Query(value=" SELECT \r\n" + 
                "  (SELECT sum(saleTotalAmount) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId and saleEndDate=CONVERT(DATE, SYSDATETIME())) as totalSales,\r\n" + 
                "  \r\n" + 
                "  (SELECT sum(saleToRefund) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId and saleEndDate=CONVERT(DATE, SYSDATETIME()))as totalRefund,\r\n" + 
                "\r\n" + 
                "  (SELECT sum(saleStateTaxAmount)\r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId and saleEndDate=CONVERT(DATE, SYSDATETIME())) as stateTax,\r\n" + 
                "\r\n" + 
                "  (SELECT sum(saleCityTaxAmount) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId and saleEndDate=CONVERT(DATE, SYSDATETIME()))  as cityTax,\r\n" + 
                "\r\n" + 
                "  (SELECT sum(saleReduceTax) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId and saleEndDate=CONVERT(DATE, SYSDATETIME()) ) as redTax", nativeQuery = true)
    public Object[] dailySummary(Long businessId);

    /**
     * Generate daily summary for a specific category.
     *
     * @param  businessId  the ID of the business
     * @return             an array containing the daily summary for the category
     */
    @Query(value="  SELECT category, sum(s.saleTotalAmount) as totalSales "+
    " FROM [RMPAY].[dbo].[ItemForSale] it join [RMPAY].[dbo].[Sale] s on it.saleID=s.saleID  where s.saleEndDate=CONVERT(DATE, SYSDATETIME()) and s.businessId=:businessId "+
    " group by category order by sum(s.saleTotalAmount) desc", nativeQuery = true)
    public Object[] dailySummaryForCategory(Long businessId);

    /**
     * Obtiene una lista de los productos mas vendidos en orden descendente
     *
     * @param  businessId	identificador del negocio
     * @return         	
     */
    @Query(value="SELECT productId, sum(it.quantity) as quantity,sum(it.quantity*it.price) as totalAmount, sum(it.grossProfit) as profit, (select top(1) name from [RMPAY].[dbo].[ItemForSale] ift where ift.productId=it.productId) as name\r\n" + //
                "  FROM [RMPAY].[dbo].[ItemForSale] it join [RMPAY].[dbo].[Sale] s on it.saleID=s.saleID  \r\n" + //
                "  where s.saleEndDate=CONVERT(DATE, SYSDATETIME()) and s.businessId=1 \r\n" + //
                "  group by productId \r\n" + //
                "  order by sum(it.quantity) desc ", nativeQuery = true)
    public Object[] dailySummaryBestSellingItems(Long businessId);
    
    /**
     * Obtiene una lista de ventas por tipo de transacción, estado, fecha y negocio al que pertenece
     *
     * @param  saleTransactionType	tipo de transacción
     * @param  saleStatus			estado de la transacción
     * @param  business	negocio que realiza la venta
     * @param  date			fecha de la venta
     * @return         	
     */
    //
    public List<Sale> findBySaleTransactionTypeAndSaleStatusAndBusinessAndSaleEndDate(String saleTransactionType, String saleStatus, Business business, LocalDate date);
}
