package com.retailmanager.rmpaydashboard.services.services.SaleService;

import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Sale;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.SaleRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.services.DTO.SaleDTO;

@Service
public class SaleService implements ISaleService {
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private TerminalRepository serviceDBTerminal;
    @Autowired
    private SaleRepository serviceDBSale;

    /**
     * Adds a sale to the database.
     *
     * @param  saleDTO   the sale to be added
     * @return           a ResponseEntity containing the added sale or an error message
     */
    @Override
    public ResponseEntity<?> addSale(SaleDTO saleDTO) {
        Business business = this.serviceDBBusiness.findOneByMerchantId(saleDTO.getMerchantId()).get();
        Terminal terminal = this.serviceDBTerminal.findById(saleDTO.getTerminalId()).get();
        Sale sale = mapper.map(saleDTO, Sale.class);
        if(business==null){
            throw new EntidadNoExisteException("El Business con merchantId "+saleDTO.getMerchantId()+" no existe en la Base de datos");
        }
        if(terminal==null){
            throw new EntidadNoExisteException("El Terminal con terminalId "+saleDTO.getTerminalId()+" no existe en la Base de datos");
        }
        
        if(sale!=null){
            try{
                sale.setTerminal(terminal);
                sale.setBusiness(business);
                sale=this.serviceDBSale.save(sale);
                saleDTO.setSaleID(sale.getSaleID());
                return new ResponseEntity<>(saleDTO,HttpStatus.CREATED);
            }catch(Exception e){
                return new ResponseEntity<>("Error: "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error",HttpStatus.BAD_REQUEST);

    }

    /**
     * Retrieves all sales for a specific merchant.
     *
     * @param  merchantId  The ID of the merchant
     * @return             ResponseEntity containing a list of SaleDTO objects with HttpStatus OK
     */
    @Override
    public ResponseEntity<?> getAllSales(String merchantId) {
        List<Sale> sales = this.serviceDBSale.findBySaleTransactionTypeAndMerchantId("SALE", merchantId);
        List<SaleDTO> salesDTO = mapper.map(sales, new TypeToken<List<SaleDTO>>() {}.getType());
        return new ResponseEntity<List<SaleDTO>>(salesDTO,HttpStatus.OK);
    }

    /**
     * Retrieves completed sales for a specific merchant.
     *
     * @param  merchantId   the ID of the merchant
     * @return              a response entity containing a list of completed sales DTOs and HTTP status OK
     */
    @Override
    public ResponseEntity<?> getCompletedSales(String merchantId) {
        List<Sale> sales = this.serviceDBSale.findBySaleTransactionTypeAndSaleStatusAndMerchantId("SALE", "SUCCEED", merchantId);
        List<SaleDTO> salesDTO = mapper.map(sales, new TypeToken<List<SaleDTO>>() {}.getType());
        return new ResponseEntity<List<SaleDTO>>(salesDTO,HttpStatus.OK);
    }

    /**
     * Retrieves completed sales within a specified date range for a given merchant.
     *
     * @param  merchantId  the ID of the merchant
     * @param  startDate   the start date of the date range
     * @param  endDate     the end date of the date range
     * @return             a ResponseEntity containing a list of completed sales within the specified date range
     */
    @Override
    public ResponseEntity<?> getCompletedSalesByDateRange(String merchantId, LocalDate startDate, LocalDate endDate) {
        List<Sale> sales = this.serviceDBSale.findBySaleEndDateBetweenAndSaleTransactionTypeAndSaleStatusAndMerchantId(startDate, endDate, "SALE", "SUCCEED", merchantId);
        List<SaleDTO> salesDTO = mapper.map(sales, new TypeToken<List<SaleDTO>>() {}.getType());
        return new ResponseEntity<List<SaleDTO>>(salesDTO,HttpStatus.OK);
    }
    
    
}
