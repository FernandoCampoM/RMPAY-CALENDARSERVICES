package com.retailmanager.rmpaydashboard.services.services.UserService;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;
import com.retailmanager.rmpaydashboard.services.DTO.UserDTO;



@Service
public class UserService implements IUserService{

    @Autowired
    private UserRepository serviceDBUser;
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    
    /** 
     * @param prmUser
     * @return ResponseEntity<?>
     */
    @Override
    @Transactional
    public ResponseEntity<?> save( UserDTO prmUser) {
        if(prmUser.getUserID()!=null){
            final boolean exist = this.serviceDBUser.existsById(prmUser.getUserID());
            if(exist){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El Usuario con userId "+prmUser.getUserID()+" ya existe en la Base de datos");
                throw objExeption;
            }else{
                prmUser.setUserID(0L);
            }
        }
        Optional<User> exist = this.serviceDBUser.findOneByUsername(prmUser.getUsername());
            if(exist.isPresent()){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El Usuario con username "+prmUser.getUsername()+" ya existe en la Base de datos");
                throw objExeption;
            }
        prmUser.setPassword(new BCryptPasswordEncoder().encode(prmUser.getPassword()));
        ResponseEntity<?> rta;
         User objUser= this.mapper.map(prmUser, User.class);
         User objUserRTA=null;
         if(objUser!=null){
            objUserRTA=this.serviceDBUser.save(objUser);
         }
        UserDTO userDTO=this.mapper.map(objUserRTA, UserDTO.class);
        if(userDTO!=null){
            userDTO.setPassword(null);
            rta=new ResponseEntity<UserDTO>(userDTO, HttpStatus.CREATED);
        }else{
            rta= new ResponseEntity<String>("Error al crear el User",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(Long userId, UserDTO prmUser) {
        ResponseEntity<?> rta;
        
        if(userId!=null){
            Optional<User> optional= this.serviceDBUser.findById(userId);
            if(optional.isPresent()==false){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El Usuario con userId "+userId+" no existe en la Base de datos");
                throw objExeption;
            }else{
                User objUser=optional.get();
                objUser.setName(prmUser.getName());
                objUser.setPassword(new BCryptPasswordEncoder().encode(prmUser.getPassword()));
                objUser.setEmail(prmUser.getEmail());
                User objUserRTA=this.serviceDBUser.save(objUser);
                UserDTO userDTO=this.mapper.map(objUserRTA, UserDTO.class);
                
                if(userDTO!=null){
                    userDTO.setPassword(null);
                    rta=new ResponseEntity<UserDTO>(userDTO, HttpStatus.OK);
                }else{
                    rta= new ResponseEntity<String>("Error al actualizar el User",HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }else{
            rta= new ResponseEntity<String>("El id del usuario es null",HttpStatus.BAD_REQUEST);
        }
        return rta;
    }

    @Override
    @Transactional
    public boolean delete(Long userId) {
        boolean bandera=false;
        
        if(userId!=null){
            Optional<User> optional= this.serviceDBUser.findById(userId);
            if(optional.isPresent()){
                User objUser=optional.get();
                if(objUser!=null){
                    this.serviceDBUser.delete(objUser);
                    bandera=true;
                }
                
            }
        }
        return bandera;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long userId) {
        if(userId!=null){
            Optional<User> optional= this.serviceDBUser.findById(userId);
            if(optional.isPresent()){
                UserDTO objUserDTO=this.mapper.map(optional.get(),UserDTO.class);
                objUserDTO.setPassword(null);
                return new ResponseEntity<UserDTO>(objUserDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Usuario con userId "+userId+" no existe en la Base de datos");
                throw objExeption;
    }

    @Override
    public ResponseEntity<?> findByUsername(String username) {
        if(username!=null){
            Optional<User> optional= this.serviceDBUser.findOneByUsername(username);
            if(optional.isPresent()){
                UserDTO objUserDTO=this.mapper.map(optional.get(),UserDTO.class);
                objUserDTO.setPassword(null);
                return new ResponseEntity<UserDTO>(objUserDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Usuario con username "+username+" no existe en la Base de datos");
                throw objExeption;
    }
    
}
