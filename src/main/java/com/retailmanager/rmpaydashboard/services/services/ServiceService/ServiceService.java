package com.retailmanager.rmpaydashboard.services.services.ServiceService;

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
import com.retailmanager.rmpaydashboard.models.Service;
import com.retailmanager.rmpaydashboard.repositories.ServiceRepository;
import com.retailmanager.rmpaydashboard.services.DTO.ServiceDTO;

@org.springframework.stereotype.Service
public class ServiceService implements IServiceService {

    @Autowired
    private ServiceRepository serviceDBService;
    
    @Autowired 
    @Qualifier("mapperbase")
    private ModelMapper mapper;

    /**
     * Save the ServiceDTO and return a ResponseEntity.
     *
     * @param  prmService  the ServiceDTO to be saved
     * @return             a ResponseEntity containing the saved ServiceDTO or an error message
     */
    @Override
    public ResponseEntity<?> save(ServiceDTO prmService) {
        Long serviceId = prmService.getServiceId();
        if(serviceId!=null){
            final boolean exist = this.serviceDBService.existsById(serviceId);
            if(exist){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El Service con serviceId "+prmService.getServiceId()+" ya existe en la Base de datos");
                throw objExeption;
            }else{
                prmService.setServiceId(null);
            }
        }
        
        ResponseEntity<?> rta;
         Service objService= this.mapper.map(prmService, Service.class);
         
         Service objUserRTA=null;
         if(objService!=null){
            objUserRTA=this.serviceDBService.save(objService);
         }
        ServiceDTO serviceDTO=this.mapper.map(objUserRTA, ServiceDTO.class);
        if(serviceDTO!=null){
            rta=new ResponseEntity<ServiceDTO>(serviceDTO, HttpStatus.CREATED);
        }else{
            rta= new ResponseEntity<String>("Error al crear el Service",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }

    /**
     * Update a service by its ID with the provided ServiceDTO.
     *
     * @param  serviceId    the ID of the service to update
     * @param  prmService   the ServiceDTO with the updated service information
     * @return              a ResponseEntity with the updated ServiceDTO or an error message
     */
    @Override
    public ResponseEntity<?> update(Long serviceId, ServiceDTO prmService) {
        ResponseEntity<?> rta=null;
        
        if(serviceId!=null){
            Optional<Service> optional= this.serviceDBService.findById(serviceId);
            if(optional.isPresent()==false){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El User con serviceId "+serviceId+" no existe en la Base de datos");
                throw objExeption;
            }else{
                Service objService=optional.get();
                if(objService!=null){
                    objService.setServiceName(prmService.getServiceName());
                    objService.setServiceDescription(prmService.getServiceDescription());
                    objService.setEnable(prmService.getEnable());
                    objService.setServiceValue(prmService.getServiceValue());
                    objService.setDuration(prmService.getDuration());
                    objService.setReferralPayment(prmService.getReferralPayment());
                    objService.setTerminals2to5(prmService.getTerminals2to5());
                    objService.setTerminals6to9(prmService.getTerminals6to9());
                    objService.setTerminals10(prmService.getTerminals10());
                    objService.setReferralPayment2to5(prmService.getReferralPayment2to5());
                    objService.setReferralPayment6to9(prmService.getReferralPayment6to9());
                    objService.setReferralPayment10(prmService.getReferralPayment10());

                    Service objUserRTA=this.serviceDBService.save(objService);
                    ServiceDTO userDTO=this.mapper.map(objUserRTA, ServiceDTO.class);
                    
                    if(userDTO!=null){
                        rta=new ResponseEntity<ServiceDTO>(userDTO, HttpStatus.OK);
                    }else{
                        rta= new ResponseEntity<String>("Error al actualizar el Service",HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
                
            }
        }else{
            rta= new ResponseEntity<String>("El id del usuario es null",HttpStatus.BAD_REQUEST);
        }
        return rta;
    }

    /**
     * Deletes a service by its ID.
     *
     * @param  serviceId	the ID of the service to be deleted
     * @return         	true if the service was successfully deleted, false otherwise
     */
    @Override
    public boolean delete(Long serviceId) {
        boolean bandera=false;
        
        if(serviceId!=null){
            Optional<Service> optional= this.serviceDBService.findById(serviceId);
            if(optional.isPresent()){
                Service objService=optional.get();
                if(objService!=null){
                    this.serviceDBService.delete(objService);
                    bandera=true;
                }
                
            }
        }
        return bandera;
    }

    /**
     * Finds a service by its ID and returns a response entity.
     *
     * @param  serviceId    the ID of the service to find
     * @return              a response entity containing the service DTO if found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long serviceId) {
        if(serviceId!=null){
            Optional<Service> optional= this.serviceDBService.findById(serviceId);
            if(optional.isPresent()){
                
                ServiceDTO objUserDTO=this.mapper.map(optional.get(),ServiceDTO.class);
                
                return new ResponseEntity<ServiceDTO>(objUserDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Servicio con serviceId "+serviceId+" no existe en la Base de datos");
                throw objExeption;
    }

    /**
     * Retrieves all service data and returns a ResponseEntity containing a list of ServiceDTO objects.
     *
     * @return          a ResponseEntity containing a list of ServiceDTO objects with HTTP status OK
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll() {
        List<ServiceDTO> listServiceDTO=new ArrayList<>();
        Iterable<Service> listService=this.serviceDBService.findAll();
        if(listService!=null){
            listServiceDTO=this.mapper.map(listService,new TypeToken<List<ServiceDTO>>(){}.getType());
        }
        return new ResponseEntity<List<ServiceDTO>>(listServiceDTO,HttpStatus.OK);
    }


    /**
     * Update the enable status of a service.
     *
     * @param  serviceId  the ID of the service to update
     * @param  enable     the new enable status
     * @return            a ResponseEntity indicating the success of the update
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateEnable(Long serviceId, boolean enable) {
        if(serviceId!=null){
            Optional<Service> optional= this.serviceDBService.findById(serviceId);
            if(optional.isPresent()){
                this.serviceDBService.updateEnable(serviceId, enable);
                return new ResponseEntity<Boolean>(true,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Servicio con serviceId "+serviceId+" no existe en la Base de datos");
                throw objExeption;
    }
    
}
