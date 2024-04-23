package com.retailmanager.rmpaydashboard.services.services.InvoiceServices;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConsumeAPIException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.FileModel;
import com.retailmanager.rmpaydashboard.models.Invoice;
import com.retailmanager.rmpaydashboard.models.Service;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.FileRepository;
import com.retailmanager.rmpaydashboard.repositories.InvoiceRepository;
import com.retailmanager.rmpaydashboard.repositories.ServiceRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.services.DTO.InvoiceDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalsDoPaymentDTO;
import com.retailmanager.rmpaydashboard.services.DTO.doPaymentDTO;
import com.retailmanager.rmpaydashboard.services.services.BusinessService.IBusinessService;
import com.retailmanager.rmpaydashboard.services.services.EmailService.EmailBodyData;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;
import com.retailmanager.rmpaydashboard.services.services.Payment.IBlackStoneService;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponsePayment;

@org.springframework.stereotype.Service
public class InvoiceServices  implements IInvoiceServices{

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private ServiceRepository serviceDBService;
    @Autowired 
    private IEmailService emailService;
    @Autowired 
    private InvoiceRepository serviceDBInvoice;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    DecimalFormat formato = new DecimalFormat("#.##");
    
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private IBusinessService businessService;
    @Autowired
    private IBlackStoneService blackStoneService;
    String msgError = "";
    @Autowired
    private TerminalRepository serviceDBTerminal;
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
    /**
     * Processes a payment request and generates an invoice.
     *
     * @param  prmPaymentInfo  the payment information DTO
     * @return                 the response entity containing the invoice DTO or an error message
     */
    @Override
    public ResponseEntity<?> doPayment(doPaymentDTO prmPaymentInfo) {
        Double totalAmount=0.0;
        ResponsePayment respPayment;
        String serviceReferenceNumber=null;
        String userTransactionNumber = uniqueString();
        Long serviceIdPrincipal=-1L;
        EmailBodyData objEmailBodyData=mapper.map(prmPaymentInfo, EmailBodyData.class);
        
        Business objBusiness = this.serviceDBBusiness.findById(prmPaymentInfo.getBusinessId()).orElse(null);
        if(objBusiness==null){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El Negocio con businessId "+prmPaymentInfo.getBusinessId()+" no existe en la Base de datos");
                throw objExeption;
        }
        try {
            prmPaymentInfo=validateData(prmPaymentInfo);
            if(prmPaymentInfo==null){
                return new ResponseEntity<String>(msgError,HttpStatus.BAD_REQUEST);
            }
            //Validar que existan todos los Servicios
                HashMap<Long,Service> listService= new HashMap<>();
                for(TerminalsDoPaymentDTO objTerminal:prmPaymentInfo.getTerminalsDoPayment()){
                    if(!listService.containsKey(objTerminal.getIdService())){
                        Optional<Service> optional= this.serviceDBService.findById(objTerminal.getIdService());
                        if(optional.isPresent()){
                            
                            listService.put(objTerminal.getIdService(), optional.get());
                        }else{
                            EntidadNoExisteException objExeption = new EntidadNoExisteException("El Servicio con serviceId "+objTerminal.getIdService()+" no existe en la Base de datos");
                            throw objExeption;
                        }
                    }
                    Terminal objTerminalDB=this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                    if(objTerminalDB==null){
                        throw new EntidadNoExisteException("El Terminal con terminalId "+objTerminal.getTerminalId()+" no existe en la Base de datos");
                    }
                    Double amount=0.0;
                    Service objService=listService.get(objTerminal.getIdService());
                    String descripcion="";
                    if(objTerminalDB.isPrincipal()){
                        descripcion="Terminal Principal ID ["+objTerminal.getTerminalId()+"] - "+objService.getServiceName()+" $"+String.valueOf(formato.format(objService.getServiceValue()))+"\n";
                        amount=objService.getServiceValue();
                        objTerminal.setPrincipal(true);
                        serviceIdPrincipal=objService.getServiceId();
                    }else{
                        objTerminal.setPrincipal(false);
                        if(prmPaymentInfo.getTerminalNumber()<=5){
                            descripcion="Terminal Adicional ID ["+objTerminal.getTerminalId()+"] - "+objService.getServiceName()+" $"+String.valueOf(formato.format(objService.getTerminals2to5()))+"\n";
                            amount=objService.getTerminals2to5();
                        }else if(prmPaymentInfo.getTerminalNumber()>5 && prmPaymentInfo.getTerminalNumber()<10){
                            descripcion="Terminal Adicional ID ["+objTerminal.getTerminalId()+"] - "+objService.getServiceName()+" $"+String.valueOf(formato.format(objService.getTerminals6to9()))+"\n";
                            amount=objService.getTerminals6to9();
                        }else{
                            descripcion="Terminal Adicional ID ["+objTerminal.getTerminalId()+"] - "+objService.getServiceName()+" $"+String.valueOf(formato.format(objService.getTerminals10()))+"\n";
                            amount=objService.getTerminals10();
                        }
                    }
                    objTerminal.setAmount(amount);
                    totalAmount+=amount;
                    objTerminal.setServiceDescription(descripcion);
                }
                objEmailBodyData.setDiscount(0.0);
                if(objBusiness.getDiscount()!=0.0 ){
                    objEmailBodyData.setReferenceNumber("DESCUENTO APLICADO:$"+String.valueOf(formato.format(objBusiness.getDiscount())));
                    objEmailBodyData.setDiscount(objBusiness.getDiscount());
                    if(objBusiness.getDiscount()<totalAmount){
                        totalAmount=totalAmount-objBusiness.getDiscount();
                    }else{
                        totalAmount=0.0;
                        prmPaymentInfo.setPaymethod("PAID-WITH-DISCOUNT");
                        objBusiness.setDiscount(objBusiness.getDiscount()-totalAmount);
                    }
                }
                objEmailBodyData.setAmount(totalAmount);
                objBusiness.setAdditionalTerminals((Integer)prmPaymentInfo.getTerminalNumber());
                if(serviceIdPrincipal!=-1L){
                    objBusiness.setServiceId(serviceIdPrincipal);
                }
                switch (prmPaymentInfo.getPaymethod()){
                    case "CREDIT-CARD":
                        respPayment=blackStoneService.paymentWithCreditCard(String.valueOf(formato.format(totalAmount)), 
                        objBusiness.getAddress().getZipcode(), 
                        prmPaymentInfo.getCreditcarnumber().replaceAll("-", ""),
                        prmPaymentInfo.getExpDateMonth() + prmPaymentInfo.getExpDateYear(), 
                        prmPaymentInfo.getNameoncard(), 
                        prmPaymentInfo.getSecuritycode(), null, userTransactionNumber);
                        if(respPayment.getResponseCode()!=200){
                            emailService.notifyErrorRegister(objEmailBodyData);
                            HashMap <String, String> objError=new HashMap<String, String>();
                            objError.put("msg", "No se pudo registrar el pago con la tarjeta de credito");
                            return new ResponseEntity<HashMap<String, String>>(objError,HttpStatus.NOT_ACCEPTABLE);
                        }
                        serviceReferenceNumber=respPayment.getServiceReferenceNumber();
                        objEmailBodyData.setReferenceNumber( serviceReferenceNumber+" - "+objEmailBodyData.getReferenceNumber());
                    break;
                }
                Invoice objInvoice=new Invoice();
                            List<Long> listTerminalIds=new ArrayList<Long>();
                            switch (prmPaymentInfo.getPaymethod()){
                                case "CREDIT-CARD":
                                objBusiness.setLastPayment(LocalDate.now());
                                    for (TerminalsDoPaymentDTO objTerminal:prmPaymentInfo.getTerminalsDoPayment()) {
                                        Service service=listService.get(objTerminal.getIdService());
                                        Terminal objTer=this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                                        objTer.setEnable(true);
                                        //se incrementa la fecha de expiración del terminal de acuerdo a la duración del servicio
                                        if(objTer.getExpirationDate()!=null && objTer.isEnable() && objTer.isPayment()){
                                            objTer.setExpirationDate(objTer.getExpirationDate().plusDays(service.getDuration()));
                                        }else{
                                            objTer.setExpirationDate(LocalDate.now().plusDays(service.getDuration()));
                                        }
                                        
                                        objTer.setPayment(true);
                                        objTer.setService(service);
                                        objTer.setAutomaticPayments(prmPaymentInfo.isAutomaticPayments());
                                        objTer=this.serviceDBTerminal.save(objTer);
                                        listTerminalIds.add(objTerminal.getTerminalId());
                                    }
                                    
                                    objInvoice.setDate(LocalDate.now());
                                    objInvoice.setTime(LocalTime.now());
                                    objInvoice.setPaymentMethod(prmPaymentInfo.getPaymethod());
                                    objInvoice.setTerminals(prmPaymentInfo.getTerminalNumber());
                                    objInvoice.setTotalAmount(totalAmount);
                                    objInvoice.setBusinessId(objBusiness.getBusinessId());
                                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                                    objInvoice.setServiceId(objBusiness.getServiceId());
                                    objInvoice.setInProcess(false);
                                    objInvoice.setTerminalIds(listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                                    objInvoice=serviceDBInvoice.save(objInvoice);
                                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                                    objEmailBodyData.setTerminalsDoPayment(prmPaymentInfo.getTerminalsDoPayment());
                                    //TODO: modificar el correo que se envia para que discrimine por cada terminal y descuento
                                    emailService.notifyPaymentCreditCard(objEmailBodyData);
                                break;
                                case "ATHMOVIL":
                                    for (TerminalsDoPaymentDTO objTerminal:prmPaymentInfo.getTerminalsDoPayment()) {
                                        Service service=listService.get(objTerminal.getIdService());
                                        Terminal objTer=this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                                        objTer.setEnable(true);
                                        //se incrementa la fecha de expiración del terminal de acuerdo a la duración del servicio
                                        if(objTer.getExpirationDate()!=null && objTer.isEnable() && objTer.isPayment()){
                                            objTer.setExpirationDate(objTer.getExpirationDate().plusDays(service.getDuration()));
                                        }else{
                                            objTer.setExpirationDate(LocalDate.now().plusDays(service.getDuration()));
                                        }
                                        
                                        objTer.setPayment(false);
                                        objTer.setService(service);
                                        
                                        objTer.setAutomaticPayments(prmPaymentInfo.isAutomaticPayments());
                                        objTer=this.serviceDBTerminal.save(objTer);
                                        listTerminalIds.add(objTerminal.getTerminalId());
                                    }
                                    
                                    objInvoice.setDate(LocalDate.now());
                                    objInvoice.setTime(LocalTime.now());
                                    objInvoice.setPaymentMethod(prmPaymentInfo.getPaymethod());
                                    objInvoice.setTerminals(prmPaymentInfo.getTerminalNumber());
                                    objInvoice.setTotalAmount(totalAmount);
                                    objInvoice.setBusinessId(objBusiness.getBusinessId());
                                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                                    objInvoice.setServiceId(objBusiness.getServiceId());
                                    objInvoice.setInProcess(true);
                                    objInvoice.setTerminalIds(listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                                    objInvoice=serviceDBInvoice.save(objInvoice);
                                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                                    objEmailBodyData.setTerminalsDoPayment(prmPaymentInfo.getTerminalsDoPayment());
                                    //TODO: modificar el correo que se envia para que discrimine por cada terminal y descuento
                                    emailService.notifyPaymentATHMovil(objEmailBodyData);
                                    break;
                                case "BANK-ACCOUNT":
                                for (TerminalsDoPaymentDTO objTerminal:prmPaymentInfo.getTerminalsDoPayment()) {
                                    Service service=listService.get(objTerminal.getIdService());
                                    Terminal objTer=this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                                    objTer.setEnable(true);
                                    //se incrementa la fecha de expiración del terminal de acuerdo a la duración del servicio
                                    if(objTer.getExpirationDate()!=null && objTer.isEnable() && objTer.isPayment()){
                                        objTer.setExpirationDate(objTer.getExpirationDate().plusDays(service.getDuration()));
                                    }else{
                                        objTer.setExpirationDate(LocalDate.now().plusDays(service.getDuration()));
                                    }
                                    
                                    objTer.setPayment(false);
                                    objTer.setService(service);
                                    
                                    objTer.setAutomaticPayments(prmPaymentInfo.isAutomaticPayments());
                                    objTer=this.serviceDBTerminal.save(objTer);
                                    listTerminalIds.add(objTerminal.getTerminalId());
                                }
                                
                                objInvoice.setDate(LocalDate.now());
                                objInvoice.setTime(LocalTime.now());
                                objInvoice.setPaymentMethod(prmPaymentInfo.getPaymethod());
                                objInvoice.setTerminals(prmPaymentInfo.getTerminalNumber());
                                objInvoice.setTotalAmount(totalAmount);
                                objInvoice.setBusinessId(objBusiness.getBusinessId());
                                objInvoice.setReferenceNumber(serviceReferenceNumber);
                                objInvoice.setServiceId(objBusiness.getServiceId());
                                objInvoice.setInProcess(true);
                                objInvoice.setTerminalIds(listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                                objInvoice=serviceDBInvoice.save(objInvoice);
                                objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                                objEmailBodyData.setTerminalsDoPayment(prmPaymentInfo.getTerminalsDoPayment());
                                //TODO: modificar el correo que se envia para que discrimine por cada terminal y descuento
                                    emailService.notifyPaymentBankAccount(objEmailBodyData);
                                break;
                                case "PAID-WITH-DISCOUNT":
                                    objBusiness.setLastPayment(LocalDate.now());
                                    for (TerminalsDoPaymentDTO objTerminal:prmPaymentInfo.getTerminalsDoPayment()) {
                                        Service service=listService.get(objTerminal.getIdService());
                                        Terminal objTer=this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                                        objTer.setEnable(true);
                                        //se incrementa la fecha de expiración del terminal de acuerdo a la duración del servicio
                                        if(objTer.getExpirationDate()!=null && objTer.isEnable() && objTer.isPayment()){
                                            objTer.setExpirationDate(objTer.getExpirationDate().plusDays(service.getDuration()));
                                        }else{
                                            objTer.setExpirationDate(LocalDate.now().plusDays(service.getDuration()));
                                        }
                                        
                                        objTer.setPayment(true);
                                        objTer.setService(service);
                                        objTer.setAutomaticPayments(prmPaymentInfo.isAutomaticPayments());
                                        objTer=this.serviceDBTerminal.save(objTer);
                                        listTerminalIds.add(objTerminal.getTerminalId());
                                    }
                                    
                                    objInvoice.setDate(LocalDate.now());
                                    objInvoice.setTime(LocalTime.now());
                                    objInvoice.setPaymentMethod(prmPaymentInfo.getPaymethod());
                                    objInvoice.setTerminals(prmPaymentInfo.getTerminalNumber());
                                    objInvoice.setTotalAmount(totalAmount);
                                    objInvoice.setBusinessId(objBusiness.getBusinessId());
                                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                                    objInvoice.setServiceId(objBusiness.getServiceId());
                                    objInvoice.setInProcess(false);
                                    objInvoice.setTerminalIds(listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                                    objInvoice=serviceDBInvoice.save(objInvoice);
                                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                                    objEmailBodyData.setTerminalsDoPayment(prmPaymentInfo.getTerminalsDoPayment());
                                    //TODO: modificar el correo que se envia para que discrimine por cada terminal y descuento
                                    //emailService.notifyPaymentCreditCard(objEmailBodyData);
                                break;
                            }
            this.serviceDBBusiness.save(objBusiness);
            InvoiceDTO objInvoiceDTO=new InvoiceDTO();
            objInvoiceDTO.setInvoiceNumber(objInvoice.getInvoiceNumber());
            return new ResponseEntity<InvoiceDTO>(objInvoiceDTO,HttpStatus.OK);
        }catch (ConsumeAPIException ex) {
                System.err.println("Error en el consumo de BlackStone: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: "+ ex.getMessage() );
                
                HashMap<String, String> map = new HashMap<>();
                map.put("msg", "Error en el consumo de BlackStone: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: "+ ex.getMessage() +"Por favor comuniquese con el administrador de la página.");
                return new ResponseEntity<HashMap<String,String>>(map,HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Generates a unique string using UUID.
     *
     * @return         the unique string generated
     */
    public String uniqueString() {
        String random = UUID.randomUUID().toString();
        random = random.replaceAll("-", "");
        random = random.substring(0, 16);

        return random;
    }
    /**
     * Validates the given RegistryDTO object based on the pay method.
     *
     * @param  prmRegistry  The RegistryDTO object to be validated
     * @return              The validated RegistryDTO object, or null if there is an error
     */
    private doPaymentDTO validateData(doPaymentDTO prmRegistry) {
        if(prmRegistry.getPaymethod()!=null && prmRegistry.getPaymethod().compareTo("CREDIT-CARD")==0){
            if(prmRegistry.getCreditcarnumber()!=null){
                if(!prmRegistry.getCreditcarnumber().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the credit card number";
                return null;
                }
            }else{
                msgError = "The credit card number is required";
                return null;
            }
            if(prmRegistry.getNameoncard()!=null){
                prmRegistry.setNameoncard(prmRegistry.getNameoncard().toUpperCase().trim());
            }else{
                msgError = "The name on card is required";
                return null;
            }
            if(prmRegistry.getSecuritycode()!=null){
                if(!prmRegistry.getSecuritycode().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the security code";
                return null;
                }
            }else{
                msgError = "The security code is required";
                return null;
            }
            if(prmRegistry.getExpDateMonth()!=null){
                if(!prmRegistry.getExpDateMonth().matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the expiration date";
                return null;
                }else{
                    if(Integer.parseInt(prmRegistry.getExpDateMonth())>12){
                        msgError = "The expiration date month must be less than or equal to 12";
                        return null;
                    }
                }
            }else{
                msgError = "The expiration date month is required";
                return null;
            }
            if(prmRegistry.getExpDateYear()!=null){
                if(!prmRegistry.getExpDateYear().matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the expiration date";
                return null;
                }else{
                    prmRegistry.setExpDateYear(prmRegistry.getExpDateYear().trim());
                    if(prmRegistry.getExpDateYear().length()!=2){
                        msgError = "The expiration date year must be 2 digits";
                        return null;
                    }
                }
            }else{
                msgError = "The expiration date year is required";
                return null;
            }
            
        }else if(prmRegistry.getPaymethod()!=null && prmRegistry.getPaymethod().compareTo("BANK-ACCOUNT")==0){
            if(prmRegistry.getChequeVoidId()!=null){
                Optional<FileModel> fileModel=this.fileRepository.findById(prmRegistry.getChequeVoidId());
                if(!fileModel.isPresent()){
                    msgError = "The cheque void file is required";
                    return null;
                }
            }
            if(prmRegistry.getAccountNameBank()!=null){
                prmRegistry.setAccountNameBank(prmRegistry.getAccountNameBank().toUpperCase().trim());
            }else{
                msgError = "The account name is required";
                return null;
            }
            if(prmRegistry.getAccountNumberBank()!=null){
                if(!prmRegistry.getAccountNumberBank().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the account number";
                    return null;
                }else{
                    prmRegistry.setAccountNumberBank(prmRegistry.getAccountNumberBank().trim());
                }
            }else{
                msgError = "The account number is required";
                return null;
            }
            if(prmRegistry.getRouteNumberBank()!=null){
                if(!prmRegistry.getRouteNumberBank().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the route number";
                    return null;
                }else{
                    prmRegistry.setRouteNumberBank(prmRegistry.getRouteNumberBank().trim());
                }
            }else{
                msgError = "The route number is required";
                return null;
            }
            if(prmRegistry.getChequeVoidId()==null){
                msgError = "The chequeVoidId is required";
                return null;
            }
            
        }
        return prmRegistry;
    }
    
}
