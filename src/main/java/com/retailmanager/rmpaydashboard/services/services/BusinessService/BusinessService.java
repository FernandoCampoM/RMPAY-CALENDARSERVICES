package com.retailmanager.rmpaydashboard.services.services.BusinessService;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;
import com.retailmanager.rmpaydashboard.services.DTO.BusinessDTO;

@Service
public class BusinessService implements IBusinessService {
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private UserRepository serviceDBUser;
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    /**
     * Save a business entity.
     *
     * @param  prmBusiness   the business DTO to be saved
     * @return               the response entity with the saved business DTO or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(BusinessDTO prmBusiness) {
        Long businessId = prmBusiness.getBusinessId();
        if(businessId!=null){
            final boolean exist = this.serviceDBBusiness.existsById(businessId);
            if(exist){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El business con businessId "+prmBusiness.getBusinessId()+" ya existe en la Base de datos");
                throw objExeption;
            }else{
                prmBusiness.setBusinessId(null);
            }
        }
        Optional<Business> exist = this.serviceDBBusiness.findOneByMerchantId(prmBusiness.getMerchantId());
            if(exist.isPresent()){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El business con merchantId "+prmBusiness.getMerchantId()+" ya existe en la Base de datos");
                throw objExeption;
            }
        
        
        ResponseEntity<?> rta;
         Business objBusiness= this.mapper.map(prmBusiness, Business.class);
         if(objBusiness!=null){
            Long userId=prmBusiness.getUserId();
            if(userId!=null){
                Optional<User> existUser = this.serviceDBUser.findById(userId);
                if(!existUser.isPresent()){
                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El User con userId "+prmBusiness.getUserId()+" no existe en la Base de datos");
                    throw objExeption;
                }else{
                    objBusiness.setUser(existUser.get());
                }
            }
            objBusiness=this.serviceDBBusiness.save(objBusiness);
         }
        BusinessDTO businessDTO=this.mapper.map(objBusiness, BusinessDTO.class);
        if(businessDTO!=null){
            
            rta=new ResponseEntity<BusinessDTO>(businessDTO, HttpStatus.CREATED);
        }else{
            rta= new ResponseEntity<String>("Error al crear el Business",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }

    /**
     * Updates the business entity with the given businessId and prmBusiness details.
     *
     * @param  businessId    the ID of the business entity to be updated
     * @param  prmBusiness   the details of the business entity for update
     * @return               the ResponseEntity with the updated business entity or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> update(Long businessId, BusinessDTO prmBusiness) {
        Business objBusiness=null;
        if(businessId!=null){
            Optional<Business> exist = this.serviceDBBusiness.findById(businessId);
            if(!exist.isPresent()){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El business con businessId "+prmBusiness.getBusinessId()+" ya existe en la Base de datos");
                throw objExeption;
            }
            objBusiness=exist.get();
        }
        if(objBusiness.getMerchantId().compareTo(prmBusiness.getMerchantId())!=0){
            Optional<Business> exist = this.serviceDBBusiness.findOneByMerchantId(prmBusiness.getMerchantId());
            if(exist.isPresent()){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El business con merchantId "+prmBusiness.getMerchantId()+" ya existe en la Base de datos");
                throw objExeption;
            }
        }
    
        ResponseEntity<?> rta;
         objBusiness.setMerchantId(prmBusiness.getMerchantId());
         objBusiness.setAdditionalTerminals(prmBusiness.getAdditionalTerminals());
         objBusiness.setBusinessPhoneNumber(prmBusiness.getBusinessPhoneNumber());
         objBusiness.getAddress().setAddress1(prmBusiness.getAddress().getAddress1());
         objBusiness.getAddress().setAddress2(prmBusiness.getAddress().getAddress2());
         objBusiness.getAddress().setCity(prmBusiness.getAddress().getCity());
         objBusiness.getAddress().setCountry(prmBusiness.getAddress().getCountry());
         objBusiness.getAddress().setZipcode(prmBusiness.getAddress().getZipcode());
         if(objBusiness!=null){
            objBusiness=this.serviceDBBusiness.save(objBusiness);
         }
        BusinessDTO businessDTO=this.mapper.map(objBusiness, BusinessDTO.class);
        if(businessDTO!=null){
            rta=new ResponseEntity<BusinessDTO>(businessDTO, HttpStatus.CREATED);
        }else{
            rta= new ResponseEntity<String>("Error al crear el Business",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }

    /**
     * Deletes a business record by its ID.
     *
     * @param  businessId   the ID of the business to delete
     * @return              true if the business was successfully deleted, false otherwise
     */
    @Override
    @Transactional
    public boolean delete(Long businessId) {
        boolean bandera=false;
        
        if(businessId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findById(businessId);
            if(optional.isPresent()){
                Business objBusiness=optional.get();
                if(objBusiness!=null){
                    this.serviceDBBusiness.delete(objBusiness);
                    bandera=true;
                }
                
            }
        }
        return bandera;
    }

    /**
     * Find a Business by its ID and return a ResponseEntity with BusinessDTO if found, 
     * otherwise throw an EntidadNoExisteException.
     *
     * @param  businessId   the ID of the Business to find
     * @return              a ResponseEntity with BusinessDTO if the Business is found
     *                      or throw an EntidadNoExisteException if not found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long businessId) {
        if(businessId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findById(businessId);
            if(optional.isPresent()){
                BusinessDTO objBusinessDTO=this.mapper.map(optional.get(),BusinessDTO.class);
                
                return new ResponseEntity<BusinessDTO>(objBusinessDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
    }

    /**
     * findByMerchantId function to retrieve Business by merchantId.
     *
     * @param  merchantId  the ID of the merchant to search for
     * @return             ResponseEntity with BusinessDTO if found, else throw exception
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findByMerchantId(String merchantId) {
        if(merchantId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findOneByMerchantId(merchantId);
            if(optional.isPresent()){
                BusinessDTO objBusinessDTO=this.mapper.map(optional.get(),BusinessDTO.class);
                
                return new ResponseEntity<BusinessDTO>(objBusinessDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con merchantId "+merchantId+" no existe en la Base de datos");
                throw objExeption;
    }
    
}
