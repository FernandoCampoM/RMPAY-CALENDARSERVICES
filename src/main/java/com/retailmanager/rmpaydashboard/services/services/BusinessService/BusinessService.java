package com.retailmanager.rmpaydashboard.services.services.BusinessService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Service;
import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.ServiceRepository;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;
import com.retailmanager.rmpaydashboard.services.DTO.BusinessDTO;
import com.retailmanager.rmpaydashboard.services.DTO.CategoryDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalDTO;

@org.springframework.stereotype.Service
public class BusinessService implements IBusinessService {
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private UserRepository serviceDBUser;
    @Autowired
    private ServiceRepository serviceDBService;
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
        Long serviceId = prmBusiness.getServiceId();
        if(serviceId!=null){
            if(serviceId!=0){
                Optional<Service> existService = this.serviceDBService.findById(serviceId);
                if(!existService.isPresent()){
                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El service con serviceId "+prmBusiness.getServiceId()+" no existe en la Base de datos");
                    throw objExeption;
                }
            }
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
        ResponseEntity<?> rta=null;
        Long serviceId = prmBusiness.getServiceId();
        if(serviceId!=null){
            if(serviceId!=0){
                Optional<Service> existService = this.serviceDBService.findById(serviceId);
                if(!existService.isPresent()){
                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El service con serviceId "+prmBusiness.getServiceId()+" no existe en la Base de datos");
                    throw objExeption;
                }
            }
        }
        if(businessId!=null){
            Optional<Business> exist = this.serviceDBBusiness.findById(businessId);
            if(!exist.isPresent()){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El business con businessId "+prmBusiness.getBusinessId()+" ya existe en la Base de datos");
                throw objExeption;
            }
            objBusiness=exist.get();
            if(objBusiness.getMerchantId().compareTo(prmBusiness.getMerchantId())!=0){
                Optional<Business> exist2 = this.serviceDBBusiness.findOneByMerchantId(prmBusiness.getMerchantId());
                if(exist2.isPresent()){
                    EntidadYaExisteException objExeption = new EntidadYaExisteException("El business con merchantId "+prmBusiness.getMerchantId()+" ya existe en la Base de datos");
                    throw objExeption;
                }
            }
             objBusiness.setMerchantId(prmBusiness.getMerchantId());
             objBusiness.setAdditionalTerminals(prmBusiness.getAdditionalTerminals());
             objBusiness.setBusinessPhoneNumber(prmBusiness.getBusinessPhoneNumber());
             objBusiness.getAddress().setAddress1(prmBusiness.getAddress().getAddress1());
             objBusiness.getAddress().setAddress2(prmBusiness.getAddress().getAddress2());
             objBusiness.getAddress().setCity(prmBusiness.getAddress().getCity());
             objBusiness.getAddress().setCountry(prmBusiness.getAddress().getCountry());
             objBusiness.getAddress().setZipcode(prmBusiness.getAddress().getZipcode());
             objBusiness.setDiscount(prmBusiness.getDiscount());
             objBusiness.setServiceId(serviceId);
             if(objBusiness!=null){
                objBusiness=this.serviceDBBusiness.save(objBusiness);
             }
            BusinessDTO businessDTO=this.mapper.map(objBusiness, BusinessDTO.class);
            if(businessDTO!=null){
                rta=new ResponseEntity<BusinessDTO>(businessDTO, HttpStatus.CREATED);
            }else{
                rta= new ResponseEntity<String>("Error al crear el Business",HttpStatus.INTERNAL_SERVER_ERROR);
            }
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

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTerminals(Long businessId) {
        List<TerminalDTO> listTerminalDTO=new ArrayList<>();
        if(businessId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findById(businessId);
            if(optional.isPresent()){
                if(optional.get().getTerminals()!=null)
                    listTerminalDTO=this.mapper.map(optional.get().getTerminals(), new TypeToken<List<TerminalDTO>>(){}.getType());
                
                return new ResponseEntity<List<TerminalDTO>>(listTerminalDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
    }

    @Override
    public ResponseEntity<?> getCategories(Long businessId) {
        List<CategoryDTO> listCategoryDTO=new ArrayList<>();
        if(businessId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findById(businessId);
            if(optional.isPresent()){
                if(optional.get().getCategories()!=null)
                    listCategoryDTO=this.mapper.map(optional.get().getCategories(),new TypeToken<List<CategoryDTO>>(){}.getType());
                return new ResponseEntity<List<CategoryDTO>>(listCategoryDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
    }

    /**
     * Update the enable status of a business.
     *
     * @param  businessId  the ID of the business to update
     * @param  enable      the new enable status
     * @return             ResponseEntity with a boolean indicating success or failure
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateEnable(Long businessId, boolean enable) {
        //TODO: Implementar validación que al actualizar el enable, el business tenga un servicio activo
        if(businessId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findById(businessId);
            if(optional.isPresent()){
                this.serviceDBBusiness.updateEnable(businessId, enable);
                return new ResponseEntity<Boolean>(true,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
    }
    
}
