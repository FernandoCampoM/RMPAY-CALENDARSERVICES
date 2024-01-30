package com.retailmanager.rmpaydashboard.services.services.TerminalService;

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
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalDTO;


@Service
public class TerminalService implements ITerminalService {
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private TerminalRepository serviceDBTerminal;
    /**
     * Saves a TerminalDTO object in the database and returns a response entity.
     *
     * @param  prmTerminal   the TerminalDTO object to be saved
     * @return               a response entity containing the saved TerminalDTO or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(TerminalDTO prmTerminal) {
        Long terminalId = prmTerminal.getTerminalId();
        if(terminalId!=null){
            final boolean exist = this.serviceDBTerminal.existsById(terminalId);
            if(exist){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El terminal con terminalId "+prmTerminal.getTerminalId()+" ya existe en la Base de datos");
                throw objExeption;
            }else{
                prmTerminal.setTerminalId(null);
            }
        }
        Optional<Terminal> exist = this.serviceDBTerminal.findOneBySerial(prmTerminal.getSerial());
            if(exist.isPresent()){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El terminal con serial "+prmTerminal.getSerial()+" ya existe en la Base de datos");
                throw objExeption;
            }
        
        
        ResponseEntity<?> rta;
         Terminal objTerminal= this.mapper.map(prmTerminal, Terminal.class);
         if(objTerminal!=null){
            Long businessId=prmTerminal.getBusinesId();
            if(businessId!=null){
                Optional<Business> existBusiness = this.serviceDBBusiness.findById(businessId);
                if(!existBusiness.isPresent()){
                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El business con businessId "+businessId+" no existe en la Base de datos");
                    throw objExeption;
                }else{
                    //TODO: Validar que el busines tenga un servicio activo, y que tenga terminales disponibles
                    objTerminal.setBusiness(existBusiness.get());
                }
            }
            objTerminal=this.serviceDBTerminal.save(objTerminal);
         }
        TerminalDTO terminalDTO=this.mapper.map(objTerminal, TerminalDTO.class);
        if(terminalDTO!=null){
            
            rta=new ResponseEntity<TerminalDTO>(terminalDTO, HttpStatus.CREATED);
        }else{
            rta= new ResponseEntity<String>("Error al crear el Terminal",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }

    /**
     * Updates a terminal in the database.
     *
     * @param  terminalId     the ID of the terminal to be updated
     * @param  prmTerminal    the updated terminal details
     * @return                response entity with the updated terminal details
     */
    @Override
    @Transactional
    public ResponseEntity<?> update(Long terminalId, TerminalDTO prmTerminal) {
        Terminal objTerminal=null;
        ResponseEntity<?> rta=null;
        if(terminalId!=null){
            Optional<Terminal> exist = this.serviceDBTerminal.findById(terminalId);
            if(!exist.isPresent()){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El terminal con terminalId "+prmTerminal.getTerminalId()+" No existe en la Base de datos");
                throw objExeption;
            }
            objTerminal=exist.get();
            if(objTerminal.getSerial().compareTo(prmTerminal.getSerial())!=0){
                Optional<Terminal> existBySerial = this.serviceDBTerminal.findOneBySerial(prmTerminal.getSerial());
                if(existBySerial.isPresent()){
                    EntidadYaExisteException objExeption = new EntidadYaExisteException("El terminal con serial "+prmTerminal.getSerial()+" ya existe en la Base de datos");
                    throw objExeption;
                }
            }
             objTerminal.setSerial(prmTerminal.getSerial());
             objTerminal.setEnable(prmTerminal.getEnable());
             objTerminal.setName(prmTerminal.getName());
             if(objTerminal!=null){
                objTerminal=this.serviceDBTerminal.save(objTerminal);
             }
            TerminalDTO terminalDTO=this.mapper.map(objTerminal, TerminalDTO.class);
            if(terminalDTO!=null){
                
                rta=new ResponseEntity<TerminalDTO>(terminalDTO, HttpStatus.OK);
            }else{
                rta= new ResponseEntity<String>("Error al actualizar el Terminal",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        return rta;
    }

    /**
     * Deletes a terminal by ID.
     *
     * @param  terminalId   the ID of the terminal to delete
     * @return              true if the terminal was deleted, false otherwise
     */
    @Override
    @Transactional
    public boolean delete(Long terminalId) {
        boolean bandera=false;
        
        if(terminalId!=null){
            Optional<Terminal> optional= this.serviceDBTerminal.findById(terminalId);
            if(optional.isPresent()){
                Terminal objTerminal=optional.get();
                if(objTerminal!=null){
                    this.serviceDBTerminal.delete(objTerminal);
                    bandera=true;
                }
                
            }
        }
        return bandera;
    }

    /**
     * Find a terminal by its ID.
     *
     * @param  terminalId    the ID of the terminal to find
     * @return               a response entity with the terminal DTO if found, or an exception if not found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long terminalId) {
        if(terminalId!=null){
            Optional<Terminal> optional= this.serviceDBTerminal.findById(terminalId);
            if(optional.isPresent()){
                TerminalDTO objTerminalDTO=this.mapper.map(optional.get(),TerminalDTO.class);
                
                return new ResponseEntity<TerminalDTO>(objTerminalDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Terminal con terminalId "+terminalId+" no existe en la Base de datos");
                throw objExeption;
    }

    /**
     * Finds a record by its serial number and returns a ResponseEntity with the 
     * corresponding TerminalDTO if found. Throws EntidadNoExisteException if the 
     * record does not exist.
     *
     * @param  serial  the serial number of the record to be found
     * @return         a ResponseEntity containing the TerminalDTO if found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findBySerial(String serial) {
        if(serial!=null){
            Optional<Terminal> optional= this.serviceDBTerminal.findOneBySerial(serial);
            if(optional.isPresent()){
                TerminalDTO objTerminalDTO=this.mapper.map(optional.get(),TerminalDTO.class);
                
                return new ResponseEntity<TerminalDTO>(objTerminalDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Terminal con serial "+serial+" no existe en la Base de datos");
                throw objExeption;
    }

    /**
     * Update the enable status of a terminal.
     *
     * @param  terminalId   the ID of the terminal to update
     * @param  enable       the new enable status
     * @return              the response entity indicating the success of the update
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateEnable(Long terminalId, boolean enable) {
        //TODO: Implementar validación que al actualizar el enable, el terminal tenga un servicio activo
        if(terminalId!=null){
            Optional<Terminal> optional= this.serviceDBTerminal.findById(terminalId);
            if(optional.isPresent()){
                this.serviceDBTerminal.updateEnable(terminalId, enable);
                return new ResponseEntity<Boolean>(true,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Terminal con terminalId "+terminalId+" no existe en la Base de datos");
                throw objExeption;
    }
    
}
