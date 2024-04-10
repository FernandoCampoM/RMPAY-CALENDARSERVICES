package com.retailmanager.rmpaydashboard.services.services.PaymentMethodService;

import java.util.ArrayList;
import java.util.List;
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

import com.retailmanager.rmpaydashboard.models.PaymentMethods;
import com.retailmanager.rmpaydashboard.repositories.PaymentMethodRepository;
import com.retailmanager.rmpaydashboard.services.DTO.PaymentMethodDTO;

@Service
public class PaymentMethosService implements IPaymentMethosService {
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private PaymentMethodRepository serviceDBPaymentMethod;
    /**
     * Save payment method.
     *
     * @param  prmPaymentMethod    payment method to be saved
     * @return                    response entity with saved payment method or error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(PaymentMethodDTO prmPaymentMethod) {
        String code = prmPaymentMethod.getCode();
        if(code!=null){
            final boolean exist = this.serviceDBPaymentMethod.existsById(code);
            if(exist){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("La El PaymentMehtod con code "+prmPaymentMethod.getCode()+" ya existe en la Base de datos");
                throw objExeption;
            }
        }
        Optional<PaymentMethods> exist = this.serviceDBPaymentMethod.findOneByName(prmPaymentMethod.getName());
        if(exist.isPresent()){
            EntidadYaExisteException objExeption = new EntidadYaExisteException("El PaymentMehtod con name "+prmPaymentMethod.getName()+" ya existe en la Base de datos");
            throw objExeption;
        }
        ResponseEntity<?> rta;
        PaymentMethods objPaymentMethod= this.mapper.map(prmPaymentMethod, PaymentMethods.class);
         if(objPaymentMethod!=null){
            
            objPaymentMethod=this.serviceDBPaymentMethod.save(objPaymentMethod);
         }
         PaymentMethodDTO categoryDTO=this.mapper.map(objPaymentMethod, PaymentMethodDTO.class);
        if(categoryDTO!=null){
            rta=new ResponseEntity<PaymentMethodDTO>(categoryDTO, HttpStatus.CREATED);
        }else{
            rta= new ResponseEntity<String>("Error al crear la category",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }

    /**
     * Update PaymentMethod.
     *
     * @param  code              The code of the PaymentMethod
     * @param  prmPaymentMethod  The PaymentMethodDTO to update
     * @return                   The ResponseEntity with the updated PaymentMethodDTO
     */
    @Override
    @Transactional
    public ResponseEntity<?> update(String code, PaymentMethodDTO prmPaymentMethod) {
        ResponseEntity<?> rta=null;
        
        if(code!=null){
            Optional<PaymentMethods> exist = this.serviceDBPaymentMethod.findById(code);
            if(exist!=null){
                if(!exist.isPresent()){
                    EntidadNoExisteException objExeption = new EntidadNoExisteException(" El PaymentMehtod con code "+prmPaymentMethod.getCode()+" no ya existe en la Base de datos");
                    throw objExeption;
                }
            }
            Optional<PaymentMethods> exist2 = this.serviceDBPaymentMethod.findOneByName(prmPaymentMethod.getName());
            if(exist2.isPresent()){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El PaymentMehtod con name "+prmPaymentMethod.getName()+" ya existe en la Base de datos");
                throw objExeption;
            }
            
            PaymentMethods objPaymentMethod=exist.get();
            objPaymentMethod.setName(prmPaymentMethod.getName());
            objPaymentMethod.setEnable(prmPaymentMethod.getEnable());
            objPaymentMethod.setNotes(prmPaymentMethod.getNotes());
            objPaymentMethod=this.serviceDBPaymentMethod.save(objPaymentMethod);
             PaymentMethodDTO categoryDTO=this.mapper.map(objPaymentMethod, PaymentMethodDTO.class);
            if(categoryDTO!=null){
                rta=new ResponseEntity<PaymentMethodDTO>(categoryDTO, HttpStatus.OK);
            }else{
                rta= new ResponseEntity<String>("Error al crear el PaymentMehtod",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        return rta;
    }

    /**
     * Deletes a record based on the provided code.
     *
     * @param  code  the code of the record to be deleted
     * @return       true if the record is successfully deleted, false otherwise
     */
    @Override
    @Transactional
    public boolean delete(String code) {
        boolean bandera=false;
        if(code!=null){
            Optional<PaymentMethods> optional= this.serviceDBPaymentMethod.findById(code);
            if(optional.isPresent()){
                PaymentMethods objPaymentMethods=optional.get();
                if(objPaymentMethods!=null){
                    this.serviceDBPaymentMethod.delete(objPaymentMethods);
                    bandera=true;
                }
                
            }
        }
        return bandera;}

    /**
     * Finds a PaymentMethod by its code.
     *
     * @param  code  the code of the PaymentMethod to find
     * @return       a ResponseEntity containing the found PaymentMethodDTO or an error message
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findByCode(String code) {
        ResponseEntity<?> rta=null;
        if(code!=null){
            Optional<PaymentMethods> exist=this.serviceDBPaymentMethod.findById(code);
        if(exist!=null){
            if(!exist.isPresent()){
                EntidadNoExisteException objExeption = new EntidadNoExisteException(" El PaymentMehtod con code "+code+" no ya existe en la Base de datos");
                throw objExeption;
            }
            PaymentMethods objPaymentMethod=exist.get();
            PaymentMethodDTO categoryDTO=this.mapper.map(objPaymentMethod, PaymentMethodDTO.class);
            if(categoryDTO!=null){
                rta=new ResponseEntity<PaymentMethodDTO>(categoryDTO, HttpStatus.OK);
            }else{
                rta= new ResponseEntity<String>("Error al crear el PaymentMehtod",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        }
        }
        
        
        return rta;
    }

    /**
     * Find a PaymentMethod by name.
     *
     * @param  name     the name of the PaymentMethod to find
     * @return          ResponseEntity containing the found PaymentMethod or an error message
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findByName(String name) {
        ResponseEntity<?> rta=null;
        Optional<PaymentMethods> exist=this.serviceDBPaymentMethod.findOneByName(name);
        if(exist!=null){
            if(!exist.isPresent()){
                EntidadNoExisteException objExeption = new EntidadNoExisteException(" El PaymentMehtod con name "+name+" no ya existe en la Base de datos");
                throw objExeption;
            }
            PaymentMethods objPaymentMethod=exist.get();
            PaymentMethodDTO categoryDTO=this.mapper.map(objPaymentMethod, PaymentMethodDTO.class);
            if(categoryDTO!=null){
                rta=new ResponseEntity<PaymentMethodDTO>(categoryDTO, HttpStatus.OK);
            }else{
                rta= new ResponseEntity<String>("Error al crear el PaymentMehtod",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        }
        
        return rta;}

    /**
     * Update the enable status for a specific code.
     *
     * @param  code   the code of the payment method to update
     * @param  enable the new enable status
     * @return        response entity with true if the update was successful
     */
    @Override
    public ResponseEntity<?> updateEnable(String code, boolean enable) {
        if(code!=null){
            Optional<PaymentMethods> optional= this.serviceDBPaymentMethod.findById(code);
            if(optional.isPresent()){
                this.serviceDBPaymentMethod.updateEnable(code, enable);
                return new ResponseEntity<Boolean>(true,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException(" El PaymentMehtod con code "+code+" no existe en la Base de datos");
                throw objExeption;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll() {
        Iterable<PaymentMethods> iterable=this.serviceDBPaymentMethod.findAll();
        List<PaymentMethodDTO> list= new ArrayList<PaymentMethodDTO>();
        iterable.forEach(objPaymentMethod->{
            list.add(this.mapper.map(objPaymentMethod, PaymentMethodDTO.class));
        });
        return new ResponseEntity<List<PaymentMethodDTO>>(list,HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findAllActives() {
        List<PaymentMethods> iterable=this.serviceDBPaymentMethod.findByEnableTrue();
        List<PaymentMethodDTO> list= new ArrayList<PaymentMethodDTO>();
        iterable.forEach(objPaymentMethod->{
            list.add(this.mapper.map(objPaymentMethod, PaymentMethodDTO.class));
        });
        return new ResponseEntity<List<PaymentMethodDTO>>(list,HttpStatus.OK);
    }

}
