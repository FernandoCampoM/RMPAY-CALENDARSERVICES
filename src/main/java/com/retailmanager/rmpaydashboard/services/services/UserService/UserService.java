package com.retailmanager.rmpaydashboard.services.services.UserService;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.enums.Rol;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;
import com.retailmanager.rmpaydashboard.services.DTO.BusinessDTO;
import com.retailmanager.rmpaydashboard.services.DTO.UserDTO;



@Service
public class UserService implements IUserService{

    @Autowired
    private UserRepository serviceDBUser;
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    
    
    /**
     * Save user data into the database and return the response entity
     *
     * @param  prmUser  the user data to be saved
     * @return          the response entity containing the saved user data or an error message
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
         if(objUser.getRol()==null){
             objUser.setRol(Rol.ROLE_USER);
         }
         User objUserRTA=null;
         if(objUser!=null){
            objUserRTA=this.serviceDBUser.save(objUser);
         }
        UserDTO userDTO=this.mapper.map(objUserRTA, UserDTO.class);
        if(userDTO!=null){
            rta=new ResponseEntity<UserDTO>(userDTO, HttpStatus.CREATED);
        }else{
            rta= new ResponseEntity<String>("Error al crear el User",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }

    /**
     * Update user information in the database.
     *
     * @param  userId   the user ID
     * @param  prmUser  the user DTO with updated information
     * @return          a response entity with the updated user DTO or an error message
     */
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

    /**
     * Deletes a user by their ID.
     *
     * @param  userId   the ID of the user to be deleted
     * @return          true if the user is successfully deleted, false otherwise
     */
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

    /**
     * Find a user by ID and return ResponseEntity with UserDTO if found, or throw an exception.
     *
     * @param  userId   the ID of the user to find
     * @return          ResponseEntity with UserDTO if user is found, or an exception if not found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long userId) {
        if(userId!=null){
            Optional<User> optional= this.serviceDBUser.findById(userId);
            if(optional.isPresent()){
                
                UserDTO objUserDTO=this.mapper.map(optional.get(),UserDTO.class);
                
                return new ResponseEntity<UserDTO>(objUserDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Usuario con userId "+userId+" no existe en la Base de datos");
                throw objExeption;
    }

    /**
     * Finds a user by username and returns a ResponseEntity with the user information.
     *
     * @param  username   the username of the user to find
     * @return            a ResponseEntity with the user information if found, otherwise throws an exception
     */
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

    /**
     * Retrieve user´s business by userId.
     *
     * @param  userId   the identifier of the user
     * @return          ResponseEntity with a list of BusinessDTO or an exception if the user does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUserBusiness(Long userId) {
        if(userId!=null){
            Optional<User> optional= this.serviceDBUser.findById(userId);
            if(optional.isPresent()){
                optional.get().getBusiness();
                List<BusinessDTO> listBusiness=this.mapper.map(optional.get().getBusiness(),new TypeToken<List<BusinessDTO>>(){}.getType());
                
                return new ResponseEntity<List<BusinessDTO>>(listBusiness,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Usuario con userId "+userId+" no existe en la Base de datos");
                throw objExeption;
    }
    
}
