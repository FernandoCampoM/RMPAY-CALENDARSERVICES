package com.retailmanager.rmpaydashboard.services.services.ResellerServices;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.BarcodeFormat;
 import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.awt.Color;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Invoice;
import com.retailmanager.rmpaydashboard.models.Reseller;
import com.retailmanager.rmpaydashboard.models.ResellerPayment;
import com.retailmanager.rmpaydashboard.models.ResellerSales;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerAcountReport;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerAcountSold;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerPaymentDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerSalesDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerUnpaidAccounts;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.ResellerPaymentRepository;
import com.retailmanager.rmpaydashboard.repositories.ResellerRepository;
import com.retailmanager.rmpaydashboard.repositories.ResellerSalesRepository;

@Service
public class ResellerServices implements IResellerService {
    @Autowired
    ResellerRepository resellerRepository;
    @Autowired
    ResellerSalesRepository resellerSalesRepository;
    @Autowired
    ResellerPaymentRepository resellerPaymentRepository;
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    @Qualifier("mapperbase")
    ModelMapper mapper;


    /**
     * Saves a new Reseller in the database.
     *
     * @param prmReseller The ResellerDTO object to be saved.
     * @return The saved ResellerDTO object with HTTP status CREATED.
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(ResellerDTO prmReseller) {
        
        Reseller objReseller=this.resellerRepository.findByUsername(prmReseller.getUsername()).orElse(null);
        if(objReseller!=null){
            throw new EntidadYaExisteException("El Reseller con username "+prmReseller.getUsername()+" ya existe en la Base de datos");
        }

        objReseller=this.mapper.map(prmReseller, Reseller.class);

        objReseller=this.resellerRepository.save(objReseller);
        return new ResponseEntity<>(this.mapper.map(objReseller, ResellerDTO.class),HttpStatus.CREATED);
    }

    /**
     * Actualiza la información de un reseller existente en la base de datos.
     *
     * @param prmReseller El objeto ResellerDTO con la información actualizada.
     * @param prmId       El identificador del reseller que se va a actualizar.
     * @return El ResellerDTO actualizado.
     */
    @Override
    @Transactional
    public ResponseEntity<?> update(ResellerDTO prmReseller, Long prmId) {
        Reseller objReseller=this.resellerRepository.findById(prmId).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con resellerId "+prmReseller.getResellerId()+" ya existe en la Base de datos");  
        }
        if(prmReseller.getUsername().compareTo(objReseller.getUsername())!=0){
            Reseller tempReseller=this.resellerRepository.findByUsername(prmReseller.getUsername()).orElse(null);
            if(tempReseller!=null){
                throw new EntidadYaExisteException("El Reseller con username "+prmReseller.getUsername()+" ya existe en la Base de datos");
            }
        }
        
        objReseller.setFirstname(prmReseller.getFirstname());
        objReseller.setLastname(prmReseller.getLastname());
        objReseller.setUsername(prmReseller.getUsername());
        objReseller.setPassword(prmReseller.getPassword());
        objReseller.setAddress(prmReseller.getAddress());
        objReseller.setCompany(prmReseller.getCompany());
        objReseller.setEmail1(prmReseller.getEmail1());
        objReseller.setEmail2(prmReseller.getEmail2());
        objReseller.setImageprofile(prmReseller.getImageprofile());
        objReseller.setPhone(prmReseller.getPhone());
        objReseller.setStatus(prmReseller.isStatus());
        objReseller=this.resellerRepository.save(objReseller);
        ResellerDTO objResellerDTO=this.mapper.map(objReseller, ResellerDTO.class);
        Double comissionsBalance=0.0;
            for(ResellerSales rs:objReseller.getSales()){
                if(rs.getResellerPayment()==null){
                    comissionsBalance=comissionsBalance+rs.getCommission();
                }
                
            }
            objResellerDTO.setCommissionsBalance(comissionsBalance);
        return new ResponseEntity<>(objResellerDTO,HttpStatus.OK);
    }

    /**
     * Retrieves a Reseller object from the database based on its identifier.
     *
     * @param prmId	The identifier of the Reseller to be retrieved.
     * @return       	A ResponseEntity containing the Reseller object mapped to ResellerDTO, or an exception if the Reseller does not exist.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> get(Long prmId) {
        Reseller objReseller=this.resellerRepository.findById(prmId).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con resellerId "+prmId+" no existe en la Base de datos");  
        }
        ResellerDTO objResellerDTO=this.mapper.map(objReseller, ResellerDTO.class);
        Double comissionsBalance=0.0;
            for(ResellerSales rs:objReseller.getSales()){
                if(rs.getResellerPayment()==null){
                    comissionsBalance=comissionsBalance+rs.getCommission();
                }
                
            }
            objResellerDTO.setCommissionsBalance(comissionsBalance);
        return new ResponseEntity<>(objResellerDTO,HttpStatus.OK);
    }

    /**
     * Retrieves a list of all Resellers from the database.
     *
     * @return A ResponseEntity containing a list of ResellerDTO objects.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll() {
        Iterable<Reseller> listReseller=this.resellerRepository.findAll();
        
        List<ResellerDTO> rtaDTO=new ArrayList<>();
        for(Reseller r:listReseller){
            ResellerDTO rDTO=this.mapper.map(r, ResellerDTO.class);
            Double comissionsBalance=0.0;
            for(ResellerSales rs:r.getSales()){
                if(rs.getResellerPayment()==null){
                    comissionsBalance=comissionsBalance+rs.getCommission();
                }
            }
            rDTO.setCommissionsBalance(comissionsBalance);
            rtaDTO.add(rDTO);
        }
        
        
        return new ResponseEntity<>(rtaDTO,HttpStatus.OK);
    }

    /**
     * Updates the status of a Reseller based on its identifier.
     *
     * @param prmId    The identifier of the Reseller to be updated.
     * @param status   The new status to be assigned to the Reseller.
     * @return         true if the update was successful, false otherwise.
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateStatus(Long prmId, boolean status) {
        Reseller objReseller=this.resellerRepository.findById(prmId).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con resellerId "+prmId+" no existe en la Base de datos");  
        }
        objReseller.setStatus(status);
        objReseller=this.resellerRepository.save(objReseller);
        return new ResponseEntity<>(true,HttpStatus.OK);
    }

    /**
     * Deletes a Reseller from the database based on its identifier.
     *
     * @param prmId	The identifier of the Reseller to be deleted.
     * @return        true if the deletion was successful, false otherwise.
     */
    @Override
    @Transactional
    public ResponseEntity<?> delete(Long prmId) {
        Reseller objReseller=this.resellerRepository.findById(prmId).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con resellerId "+prmId+" no existe en la Base de datos");  
        }
        this.resellerRepository.delete(objReseller);
        return new ResponseEntity<>(true,HttpStatus.OK);
    }

    /**
     * Verifica si un reseller con el identificador proporcionado existe en la base de datos.
     *
     * @param  prmId  El identificador del reseller que se desea verificar.
     * @return        true si el reseller existe, false en caso contrario.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> exist(Long prmId) {
        return new ResponseEntity<>(this.resellerRepository.existsById(prmId),HttpStatus.OK);
    }

    /**
     * Busca un Reseller por nombre de usuario.
     *
     * @param userName El nombre de usuario del Reseller que se desea buscar.
     * @return Un objeto ResponseEntity que contiene los detalles del Reseller encontrado.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findByUsername(String userName) {
        Reseller objReseller=this.resellerRepository.findByUsername(userName).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con username "+userName+" no existe en la Base de datos");  
        }
        ResellerDTO objResellerDTO=this.mapper.map(objReseller, ResellerDTO.class);
        Double comissionsBalance=0.0;
            for(ResellerSales rs:objReseller.getSales()){
                if(rs.getResellerPayment()==null){
                    comissionsBalance=comissionsBalance+rs.getCommission();
                }
                
            }
            objResellerDTO.setCommissionsBalance(comissionsBalance);
        return new ResponseEntity<>(objResellerDTO,HttpStatus.OK);
    }

    /**
     * Retrieves a list of ResellerAcountSold objects associated with a given Reseller.
     *
     * @param  prmResellerId   the identifier of the Reseller
     * @return                  a ResponseEntity containing a list of ResellerAcountSold objects
     *                          and the HTTP status of the operation
     * @throws EntidadNoExisteException if the Reseller with the given identifier does not exist
     */
    @Override
    @Transactional
    public ResponseEntity<?> getAccountsSold(Long prmResellerId) {
        try{
            Reseller objReseller=this.resellerRepository.findById(prmResellerId).orElse(null);
            if(objReseller==null){
                throw new EntidadNoExisteException("El Reseller con resellerId "+prmResellerId+" no existe en la Base de datos");
            }
            List<ResellerSales> listResellerSales=objReseller.getSales();
            List<ResellerAcountSold> listResellerAcountSold=new ArrayList<>();

            for(ResellerSales objResellerSales:listResellerSales){
                Business objBusiness=this.businessRepository.findOneByMerchantId(objResellerSales.getMerchantId()).orElse(null);
                if(objBusiness!=null){
                    ResellerAcountSold objResellerAcountSold=new ResellerAcountSold();
                    objResellerAcountSold.setResellerSalesId(objResellerSales.getResellerSalesId());
                    objResellerAcountSold.setMerchantId(objResellerSales.getMerchantId());
                    objResellerAcountSold.setMerchantName(objBusiness.getName());
                    for(Terminal objTerminal:objBusiness.getTerminals()){
                        if(objTerminal.isPrincipal()){
                            objResellerAcountSold.setLastTransmission(objTerminal.getLastTransmision());
                            objResellerAcountSold.setExpirationDate(objTerminal.getExpirationDate());
                            break;
                        }
                    }
                    listResellerAcountSold.add(objResellerAcountSold);
                }
            }
            return new ResponseEntity<>(listResellerAcountSold,HttpStatus.OK);
        }catch(Exception e){
            System.out.println("com.retailmanager.rmpaydashboard.services.services.ResellerServices.ResellerServices.getAccountsSold "+e.getMessage());
            HashMap<String, String> map = new HashMap<>();
            map.put("message", e.getMessage());
            return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR); 
        }
    }

    /**
     * Generates a QR code for a given Reseller ID and returns it as a ResponseEntity.
     *
     * @param prmResellerId	The ID of the Reseller for whom the QR code is to be generated
     * @return         	A ResponseEntity containing the QR code as a byte array and the HTTP status of the operation
     */
    @Override
    @Transactional
    public ResponseEntity<?> getQRcode(Long prmResellerId) {
         try{
            String text="rmpay.ivucontrolpr.com/register/";
            int width = 1024;
            int height = 1024;
            Reseller objReseller=this.resellerRepository.findById(prmResellerId).orElse(null);
            if(objReseller==null){
                throw new EntidadNoExisteException("El Reseller con resellerId "+prmResellerId+" no existe en la Base de datos");
            }
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-1");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text+objReseller.getUsername(), BarcodeFormat.QR_CODE, width, height,hints);
            
            BufferedImage bufferedImage = toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageData = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG); // Cambia esto si necesitas otro tipo de imagen

            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
           
            
        }catch(Exception e){
            System.out.println("com.retailmanager.rmpaydashboard.services.services.ResellerServices.getQRcode() "+e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private static BufferedImage toBufferedImage(BitMatrix byteMatrix) {
        int width = byteMatrix.getWidth();
        int height = byteMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, byteMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                
            }
        }
        return image;
    }

    /**
     * Retrieves a list of reseller account reports.
     * 
     * This function fetches a list of reseller sales from the repository, 
     * constructs a list of ResellerAcountReport objects, and returns them 
     * as a ResponseEntity with a status of OK.
     * 
     * If an exception occurs during the execution of this function, 
     * it catches the exception, logs the error, and returns a ResponseEntity 
     * with a status of INTERNAL_SERVER_ERROR and a map containing an error message.
     * 
     * @return A ResponseEntity containing a list of ResellerAcountReport objects or an error message.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAccountsReport() {
        try{
            List<ResellerSales> listResellerSales=this.resellerSalesRepository.getAllResellers();
            List<ResellerAcountReport> listResellerAcountReport=new ArrayList<>();
            for(ResellerSales objResellerSales:listResellerSales){
                Business objBusiness=this.businessRepository.findOneByMerchantId(objResellerSales.getMerchantId()).orElse(null);
                if(objBusiness!=null){
                    ResellerAcountReport objResellerAcountReport=new ResellerAcountReport();
                    objResellerAcountReport.setResellerName(objResellerSales.getReseller().getFirstname()+" "+objResellerSales.getReseller().getLastname());
                    objResellerAcountReport.setResellerSalesId(objResellerSales.getResellerSalesId());
                    objResellerAcountReport.setResellerId(objResellerSales.getReseller().getResellerId());
                    objResellerAcountReport.setMerchantId(objResellerSales.getMerchantId());
                    objResellerAcountReport.setClientName(objBusiness.getUser().getName());
                    objResellerAcountReport.setCommission(objResellerSales.getCommission());
                    objResellerAcountReport.setDetailService(objResellerSales.getDetailService());
                    objResellerAcountReport.setTotalService(objResellerSales.getTotalService());
                    objResellerAcountReport.setPaymentDate(objResellerSales.getPaymentDate());
                    
                    listResellerAcountReport.add(objResellerAcountReport);
                }
            }
            return new ResponseEntity<List<ResellerAcountReport>>(listResellerAcountReport,HttpStatus.OK);
        }catch(Exception e){
            System.out.println("Error en com.retailmanager.rmpaydashboard.services.services.ResellerServices.getAccountsReport: "+e.getMessage());
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Error al generar el reporte de cuentas. Por favor comuniquese con el administrador de la página."+e.getMessage());
            return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    /**
     * Retrieves a list of ResellerAcountReport objects associated with the given resellerId.
     *
     * @param  prmResellerId  the ID of the reseller
     * @return          a ResponseEntity containing the list of ResellerAcountReport objects or an error message
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAccountsReport(Long prmResellerId) {
        try{
            List<ResellerSales> listResellerSales=this.resellerSalesRepository.getAllBy(prmResellerId);
            List<ResellerAcountReport> listResellerAcountReport=new ArrayList<>();
            for(ResellerSales objResellerSales:listResellerSales){
                Business objBusiness=this.businessRepository.findOneByMerchantId(objResellerSales.getMerchantId()).orElse(null);
                if(objBusiness!=null){
                    ResellerAcountReport objResellerAcountReport=new ResellerAcountReport();
                    objResellerAcountReport.setResellerName(objResellerSales.getReseller().getFirstname()+" "+objResellerSales.getReseller().getLastname());
                    objResellerAcountReport.setResellerSalesId(objResellerSales.getResellerSalesId());
                    objResellerAcountReport.setResellerId(objResellerSales.getReseller().getResellerId());
                    objResellerAcountReport.setMerchantId(objResellerSales.getMerchantId());
                    objResellerAcountReport.setClientName(objBusiness.getUser().getName());
                    objResellerAcountReport.setCommission(objResellerSales.getCommission());
                    objResellerAcountReport.setDetailService(objResellerSales.getDetailService());
                    objResellerAcountReport.setTotalService(objResellerSales.getTotalService());
                    objResellerAcountReport.setPaymentDate(objResellerSales.getPaymentDate());
                    
                    listResellerAcountReport.add(objResellerAcountReport);
                }
            }
            return new ResponseEntity<List<ResellerAcountReport>>(listResellerAcountReport,HttpStatus.OK);
        }catch(Exception e){
            System.out.println("Error en com.retailmanager.rmpaydashboard.services.services.ResellerServices.getAccountsReport("+prmResellerId+"): "+e.getMessage());
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Error al generar el reporte de cuentas. Por favor comuniquese con el administrador de la página."+e.getMessage());
            return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns a list of unpaid accounts for a given reseller.
     *
     * @param prmResellerId	The ID of the reseller.
     * @return         	A ResponseEntity containing a list of ResellerUnpaidAccounts.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUnpaidAccounts(Long prmResellerId) {
        try{
            List<ResellerSales> listResellerSales=this.resellerSalesRepository.getAllBy(prmResellerId);
            List<ResellerUnpaidAccounts> listResellerAcountReport=new ArrayList<>();
            for(ResellerSales objResellerSales:listResellerSales){
                Business objBusiness=this.businessRepository.findOneByMerchantId(objResellerSales.getMerchantId()).orElse(null);
                if(objBusiness!=null && objResellerSales.getInvoice().isInProcess()==false && objResellerSales.getResellerPayment()==null){
                    ResellerUnpaidAccounts objResellerAcountReport=new ResellerUnpaidAccounts();
                    objResellerAcountReport.setResellerSalesId(objResellerSales.getResellerSalesId());
                    objResellerAcountReport.setIdUser(objBusiness.getUser().getUserID());
                    objResellerAcountReport.setClientName(objBusiness.getUser().getName());
                    objResellerAcountReport.setCommission(objResellerSales.getCommission());
                    objResellerAcountReport.setTotalService(objResellerSales.getTotalService());
                    
                    listResellerAcountReport.add(objResellerAcountReport);
                }
            }
            return new ResponseEntity<List<ResellerUnpaidAccounts>>(listResellerAcountReport,HttpStatus.OK);
        }catch(Exception e){
            System.out.println("Error en com.retailmanager.rmpaydashboard.services.services.ResellerServices.getUnpaidAccounts("+prmResellerId+"): "+e.getMessage());
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Error al generar el reporte de cuentas. Por favor comuniquese con el administrador de la página."+e.getMessage());
            return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Realiza un pago asociado a un revendedor.
     *
     * @param prmPayment Objeto ResellerPaymentDTO que contiene la información del pago.
     * @return ResponseEntity con Objeto ResellerPaymentDTO actualizado con la información del pago realizado.
     */
    @Override
    @Transactional
    public ResponseEntity<?> doPayment(ResellerPaymentDTO prmPayment) {
        Reseller objReseller=this.resellerRepository.findById(prmPayment.getResellerId()).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con ID "+prmPayment.getResellerId()+" no existe en la Base de datos");
        }
        List<Long> reselelrSalesId=new ArrayList<>();
        try{
            ResellerPayment objResellerPayment=new ResellerPayment();
        objResellerPayment.setDate(prmPayment.getDate());
        objResellerPayment.setTime(prmPayment.getTime());
        objResellerPayment.setPaymentMethod(prmPayment.getPaymentMethod());
        objResellerPayment.setTotal(prmPayment.getTotal());
        objResellerPayment.setReseller(objReseller);

        objResellerPayment=this.resellerPaymentRepository.save(objResellerPayment);
        
        Iterable<ResellerSales> listResellerSales=this.resellerSalesRepository.findAllById(prmPayment.getResellerSalesId());
        for(ResellerSales objResellerSales:listResellerSales){
            if(objResellerSales!=null && objResellerSales.getInvoice().isInProcess()==false){
                if (objResellerSales.getResellerPayment() != null) {
                    continue; // Si ya tiene un paymentId, omitir la actualización para evitar conflicto
                }
                this.resellerSalesRepository.UpdatePaymentDateAndResellerPayment(objResellerSales.getResellerSalesId(), prmPayment.getDate(), objResellerPayment);
                reselelrSalesId.add(objResellerSales.getResellerSalesId());
            }
        }
        prmPayment.setResellerSalesId(reselelrSalesId);
        prmPayment.setPaymentId(objResellerPayment.getPaymentId());
        
        }catch(Exception e){
            System.out.println("Error en com.retailmanager.rmpaydashboard.services.services.ResellerServices.doPayment(): "+e.getMessage());
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Error al realizar el pago. Por favor comuniquese con el administrador de la página."+e.getMessage());
            return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(reselelrSalesId.isEmpty()){
            throw new EntidadNoExisteException("No existen ventas para realizar el pago");
        }
        
        return new ResponseEntity<>(prmPayment,HttpStatus.OK);
    }

    /**
     * Adds ResellerSales to the database, given the Reseller ID, Merchant ID, total value, commission, invoice, and service description.
     *
     * @param prmResellerId The ID of the Reseller.
     * @param prmMerchantId The ID of the Merchant.
     * @param prmTotalValue The total value of the ResellerSales.
     * @param prmCommission The commission of the ResellerSales.
     * @param prmInvoice The invoice of the ResellerSales.
     * @param prmServiceDescription The description of the ResellerSales.
     * @return A ResponseEntity containing the ResellerSales DTO and the HTTP status of the operation.
     */
    @Override
    @Transactional
    public ResponseEntity<?> addResellerSales(Long prmResellerId, String prmMerchantId,Double prmTotalValue, Double prmCommission,
            Invoice prmInvoice, String prmServiceDescription) {
                Reseller objReseller=this.resellerRepository.findById(prmResellerId).orElse(null);
                if(objReseller==null){
                    return null;
                }
                if(!objReseller.isStatus()){
                    prmCommission=0.0;
                }
                ResellerSales objRS= new ResellerSales();
                objRS.setReseller(objReseller);
                objRS.setMerchantId(prmMerchantId);
                objRS.setCommission(prmCommission);
                objRS.setDetailService(prmServiceDescription);
                objRS.setTotalService(prmTotalValue);
                objRS.setInvoice(prmInvoice);
                this.resellerSalesRepository.save(objRS);
                ResellerSalesDTO objRSdto=new ResellerSalesDTO();
                objRSdto.setResellerSalesId(objRS.getResellerSalesId());
                objRSdto.setCommission(prmCommission);
                objRSdto.setMerchantId(prmMerchantId);
                objRSdto.setDetailService(prmServiceDescription);
                objRSdto.setTotalService(prmTotalValue);
                objRSdto.setResellerId(prmResellerId);
                objRSdto.setInvoiceId(prmInvoice.getInvoiceNumber());
                return new ResponseEntity<>(objRSdto,HttpStatus.OK);
    }

    /**
     * Retrieves the payment history for a given reseller.
     *
     * This function fetches a list of reseller payments from the repository, 
     * constructs a list of ResellerPaymentDTO objects, and returns them 
     * as a ResponseEntity with a status of OK.
     *
     * If an exception occurs during the execution of this function, 
     * it catches the exception, logs the error, and returns a ResponseEntity 
     * with a status of INTERNAL_SERVER_ERROR and a map containing an error message.
     *
     * @param prmResellerId	The ID of the reseller.
     * @return         	A ResponseEntity containing a list of ResellerPaymentDTO objects or an error message.
     */
    @Override
    @Transactional
    public ResponseEntity<?> getPaymentHistory(Long prmResellerId) {
        Reseller objReseller=this.resellerRepository.findById(prmResellerId).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con ID "+prmResellerId+" no existe en la Base de datos");
        }
        try{
            List<ResellerPaymentDTO> objRPSL=new ArrayList<>();
            List<ResellerPayment> objRPS=this.resellerPaymentRepository.getAllBy(prmResellerId);
            for(ResellerPayment objRP:objRPS){
                ResellerPaymentDTO objRPSdto=new ResellerPaymentDTO();
                objRPSdto.setPaymentId(objRP.getPaymentId());
                objRPSdto.setDate(objRP.getDate());
                objRPSdto.setTime(objRP.getTime().withNano(0));
                objRPSdto.setPaymentMethod(objRP.getPaymentMethod());
                objRPSdto.setTotal(objRP.getTotal());
                objRPSdto.setResellerId(objRP.getReseller().getResellerId());
                objRPSdto.setSalesInfo(new ArrayList<>());
                HashMap<String, String> salesInfo = new HashMap<>();
                for(ResellerSales objRS:objRP.getResellerSales()){
                    Business objBusiness=this.businessRepository.findOneByMerchantId(objRS.getMerchantId()).orElse(null);
                    if(objBusiness!=null){
                        salesInfo.put("clientName",objBusiness.getUser().getName());
                    }
                    salesInfo.put("commission",String.valueOf(objRS.getCommission()));
                    salesInfo.put("resellerSalesId",objRS.getResellerSalesId().toString());
                    objRPSdto.getSalesInfo().add(salesInfo);
                }
                objRPSL.add(objRPSdto);
            }
            return new ResponseEntity<>(objRPSL,HttpStatus.OK);
        }catch(Exception e){
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Error al obtener el historial de pagos. Por favor comuniquese con el administrador de la página."+e.getMessage());
            return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    @Override
    @Transactional
    public ResponseEntity<?> getPaymentHistory(Long prmResellerId, LocalDate startDate, LocalDate endDate) {
        Reseller objReseller=this.resellerRepository.findById(prmResellerId).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con ID "+prmResellerId+" no existe en la Base de datos");
        }
        try{
            List<ResellerPaymentDTO> objRPSL=new ArrayList<>();
            List<ResellerPayment> objRPS=this.resellerPaymentRepository.getAllBy(prmResellerId,startDate,endDate);
            for(ResellerPayment objRP:objRPS){
                ResellerPaymentDTO objRPSdto=new ResellerPaymentDTO();
                objRPSdto.setPaymentId(objRP.getPaymentId());
                objRPSdto.setDate(objRP.getDate());
                objRPSdto.setTime(objRP.getTime().withNano(0));
                objRPSdto.setPaymentMethod(objRP.getPaymentMethod());
                objRPSdto.setTotal(objRP.getTotal());
                objRPSdto.setResellerId(objRP.getReseller().getResellerId());
                objRPSdto.setSalesInfo(new ArrayList<>());
                HashMap<String, String> salesInfo = new HashMap<>();
                for(ResellerSales objRS:objRP.getResellerSales()){
                    Business objBusiness=this.businessRepository.findOneByMerchantId(objRS.getMerchantId()).orElse(null);
                    if(objBusiness!=null){
                        salesInfo.put("clientName",objBusiness.getUser().getName());
                    }
                    salesInfo.put("commission",String.valueOf(objRS.getCommission()));
                    salesInfo.put("resellerSalesId",objRS.getResellerSalesId().toString());
                    objRPSdto.getSalesInfo().add(salesInfo);
                }
                objRPSL.add(objRPSdto);
            }
            return new ResponseEntity<>(objRPSL,HttpStatus.OK);
        }catch(Exception e){
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Error al obtener el historial de pagos. Por favor comuniquese con el administrador de la página."+e.getMessage());
            return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
