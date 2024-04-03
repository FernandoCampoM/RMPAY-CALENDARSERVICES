package com.retailmanager.rmpaydashboard.services.services.InvoiceServices;

import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.models.Invoice;
import com.retailmanager.rmpaydashboard.repositories.InvoiceRepository;
import com.retailmanager.rmpaydashboard.services.DTO.InvoiceDTO;

@Service
public class InvoiceServices  implements IInvoiceServices{

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    /**
     * Retrieves the payment history for a given business within a specified date range.
     *
     * @param  businessId   the ID of the business
     * @param  startDate    the start date of the payment history
     * @param  endDate      the end date of the payment history
     * @return              a ResponseEntity containing the payment history or an error message
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPaymentHistoryByBusiness(Long businessId, LocalDate startDate, LocalDate endDate) {
        if(businessId!=null && startDate!=null && endDate!=null){   
            List<Invoice> listInvoice=this.invoiceRepository.findByBusinessIdAndDateGreaterThanEqualAndDateLessThanEqual(businessId, startDate, endDate);
            List<InvoiceDTO> listInvoiceDTO=this.mapper.map(listInvoice, new TypeToken<List<InvoiceDTO>>() {}.getType());
            return new ResponseEntity<>(listInvoiceDTO,HttpStatus.OK);
        }
        return new ResponseEntity<>("{\"message\":\"Debe especificar businessId, startDate y endDate\"}",HttpStatus.BAD_REQUEST);
    }
    
}
