package com.retailmanager.rmpaydashboard.services.services.UsersBusinessService;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Permission;
import com.retailmanager.rmpaydashboard.models.UserBusiness_Category;
import com.retailmanager.rmpaydashboard.models.UserBusiness_Product;
import com.retailmanager.rmpaydashboard.models.UserPermission;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.PermisionRepository;
import com.retailmanager.rmpaydashboard.repositories.UserBusiness_CategoryRepository;
import com.retailmanager.rmpaydashboard.repositories.UserBusiness_ProductRepository;
import com.retailmanager.rmpaydashboard.repositories.UserPermissionRepository;
import com.retailmanager.rmpaydashboard.repositories.UsersAppRepository;
import com.retailmanager.rmpaydashboard.services.DTO.CategoryDTO;
import com.retailmanager.rmpaydashboard.services.DTO.PermissionDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;
import com.retailmanager.rmpaydashboard.services.DTO.UsersBusinessDTO;

@Service
public class UsersBusinesService implements IUsersBusinessService{
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private UsersAppRepository usersAppDBService;
    @Autowired
    private UserBusiness_ProductRepository ubpServices;
    @Autowired
    private UserBusiness_CategoryRepository ubcServices;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private PermisionRepository serviceDBUPermission;
    @Autowired
    private UserPermissionRepository serviceDBUserPermission;
    /**
     * Save the UsersBusinessDTO to the database.
     *
     * @param  prmUsersBusiness	the UsersBusinessDTO to be saved
     * @return         			ResponseEntity containing the saved UsersBusinessDTO or a BAD_REQUEST status
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(UsersBusinessDTO prmUsersBusiness) {
        Long businessId=prmUsersBusiness.getIdBusines();
        Business business = null;
        if(businessId!=null) {
            business=this.serviceDBBusiness.findById(businessId).orElse(null);
            UsersBusiness usersBusiness = this.mapper.map(prmUsersBusiness, UsersBusiness.class);
            usersBusiness.setUserPermissions(new ArrayList<>());
            if(business!=null) {
                usersBusiness.setBusiness(business);
                for(Long idPermission:prmUsersBusiness.getActivesPermissions()){
                    
                    Permission permission = this.serviceDBUPermission.findById(idPermission).orElse(null);
                    if(permission==null){
                        throw new EntidadNoExisteException("El Permission con permissionId "+idPermission+" no existe en la Base de datos");
                    }
                    UserPermission userPermission = new UserPermission();
                    userPermission.setPermission(permission);
                    userPermission.setUserBusiness(usersBusiness);
                    userPermission.setEnable(true);
                    usersBusiness.getUserPermissions().add(userPermission);

                }
                usersBusiness=this.usersAppDBService.save(usersBusiness);
                return new ResponseEntity<UsersBusinessDTO>(this.mapper.map(usersBusiness, UsersBusinessDTO.class), HttpStatus.CREATED);
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(Long userBusinessId, UsersBusinessDTO prmUsersBusiness) {
        if(userBusinessId!=null) {
            UsersBusiness usersBusiness = this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(usersBusiness!=null) {
                usersBusiness.setUsername(prmUsersBusiness.getUsername());
                usersBusiness.setPassword(prmUsersBusiness.getPassword());
                usersBusiness.setEnable(prmUsersBusiness.getEnable());
                usersBusiness.getUserPermissions().clear();
                for(Long idPermission:prmUsersBusiness.getActivesPermissions()){
                    
                    Permission permission = this.serviceDBUPermission.findById(idPermission).orElse(null);
                    if(permission==null){
                        throw new EntidadNoExisteException("El Permission con permissionId "+idPermission+" no existe en la Base de datos");
                    }
                    UserPermission userPermission = new UserPermission();
                    userPermission.setPermission(permission);
                    userPermission.setUserBusiness(usersBusiness);
                    usersBusiness.getUserPermissions().add(userPermission);
                } 
                usersBusiness=this.usersAppDBService.save(usersBusiness);
                prmUsersBusiness=this.mapper.map(usersBusiness, UsersBusinessDTO.class);
                prmUsersBusiness.setActivesPermissions(new ArrayList<>());
                for (UserPermission iterable_element : usersBusiness.getUserPermissions()) {
                    prmUsersBusiness.getActivesPermissions().add(iterable_element.getPermission().getPermissionId());
                }
                return new ResponseEntity<UsersBusinessDTO>(prmUsersBusiness, HttpStatus.OK);
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Delete a userBusiness by userBusinessId.
     *
     * @param  userBusinessId  the ID of the userBusiness to be deleted
     * @return                 true if the userBusiness is deleted, false otherwise
     */
    @Override
    @Transactional
    public boolean delete(Long userBusinessId) {
        if(userBusinessId!=null) {
            UsersBusiness usersBusiness = this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(usersBusiness!=null) {
                this.usersAppDBService.delete(usersBusiness);
                return true;
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return false;
    }

    /**
     * Finds a user by their business ID.
     *
     * @param  userBusinessId	the ID of the user's business
     * @return         		the ResponseEntity containing the user's business details or a BAD_REQUEST response
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long userBusinessId) {
        if(userBusinessId!=null) {
            UsersBusiness usersBusiness = this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(usersBusiness!=null) {
                UsersBusinessDTO user=this.mapper.map(usersBusiness, UsersBusinessDTO.class);
                user.setActivesPermissions(new ArrayList<>());
                for (UserPermission userPermission : usersBusiness.getUserPermissions()) {
                    user.getActivesPermissions().add(userPermission.getPermission().getPermissionId());
                }
                return new ResponseEntity<UsersBusinessDTO>(user, HttpStatus.OK);
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Updates the enable status for a user business.
     *
     * @param  userBusinessId   the ID of the user's business
     * @param  enable           the new enable status
     * @return                 a ResponseEntity with the updated UsersBusinessDTO and HttpStatus
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateEnable(Long userBusinessId, boolean enable) {
        if(userBusinessId!=null) {
            UsersBusiness usersBusiness = this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(usersBusiness!=null) {
                usersBusiness.setEnable(enable);
                usersBusiness=this.usersAppDBService.save(usersBusiness);
                return new ResponseEntity<Boolean>(true, HttpStatus.OK);
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Find and return users by business ID.
     *
     * @param  idBusiness   the ID of the business
     * @return              the ResponseEntity containing the list of users business DTOs
     */
    @Override
    public ResponseEntity<?> findByBusiness(Long idBusiness) {
        if(idBusiness!=null) {
            Business business = this.serviceDBBusiness.findById(idBusiness).orElse(null);
            if(business!=null) {
                List<UsersBusiness> usersBusiness = this.usersAppDBService.findByBusiness(business);
                List<UsersBusinessDTO> usersBusinessDTO = this.mapper.map(usersBusiness,  new TypeToken<List<UsersBusinessDTO>>(){}.getType());
                for(int i=0;i<usersBusiness.size();i++){
                    usersBusinessDTO.get(i).setActivesPermissions(new ArrayList<>()   );{
                    for(UserPermission up:usersBusiness.get(i).getUserPermissions()){
                        usersBusinessDTO.get(i).getActivesPermissions().add(up.getPermission().getPermissionId());
                    }
                    }
                }
                return new ResponseEntity<List<UsersBusinessDTO>>(usersBusinessDTO, HttpStatus.OK);
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+idBusiness+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Updates the permission of a user.
     *
     * @param  idUser           the ID of the user
     * @param  idPermission     the ID of the permission
     * @param  enable           the enable status of the permission
     * @return                  a ResponseEntity with a Boolean value and HttpStatus
     *                          HttpStatus.OK if the permission is updated successfully
     *                          HttpStatus.BAD_REQUEST if the user or permission does not exist
     * @throws EntidadNoExisteException if the permission does not exist in the database
     */
    @Override
    @Transactional
    public ResponseEntity<?> updatePermission(Long idUser, Long idPermission, boolean enable) {
        if(idUser!=null) {
            UsersBusiness usersBusiness = this.usersAppDBService.findById(idUser).orElse(null);
            if(usersBusiness!=null) {
                for (UserPermission userPermission : usersBusiness.getUserPermissions()) {
                    if (userPermission.getPermission().getPermissionId()==idPermission) {
                        if(enable){
                            userPermission.setEnable(true);
                        }else{
                            usersBusiness.getUserPermissions().remove(userPermission);
                            this.usersAppDBService.save(usersBusiness);
                            //this.serviceDBUserPermission.delete(userPermission);
                        }
                        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
                    }
                }
                if(enable){
                    Permission permission = this.serviceDBUPermission.findById(idPermission).orElse(null);
                    if(permission==null){
                        throw new EntidadNoExisteException("El Permission con permissionId "+idPermission+" no existe en la Base de datos");

                    }
                    UserPermission userPermission = new UserPermission();
                    userPermission.setPermission(permission);
                    userPermission.setUserBusiness(usersBusiness);
                    userPermission.setEnable(true);
                    this.serviceDBUserPermission.save(userPermission);
                    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
                }else{
                    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
                }
                
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El UsersBusiness con userBusinessId "+idUser+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Retrieves all permissions from the database and returns them as a list of PermissionDTO objects.
     *
     * @return         	A ResponseEntity object containing a list of PermissionDTO objects and an HTTP status code.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllPermissions() {
        Iterable<Permission> permissions = this.serviceDBUPermission.findAll();
        List<PermissionDTO> permissionsDTO = this.mapper.map(permissions,  new TypeToken<List<PermissionDTO>>(){}.getType());
        return new ResponseEntity<List<PermissionDTO>>(permissionsDTO, HttpStatus.OK);
    }

    /**
     * Retrieves the list of products associated with the given user business ID.
     *
     * @param  userBusinessId  the ID of the user business
     * @return                 the ResponseEntity containing the list of ProductDTO objects
     *                         or an INTERNAL_SERVER_ERROR status if an exception occurs
     * @throws EntidadNoExisteException if the UsersBusiness with the given ID does not exist in the database
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProducts(Long userBusinessId) {
        List<ProductDTO> productDTOs=new ArrayList<>();
        
            UsersBusiness objUser=this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(objUser==null){
                throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
            }
            try {    
            List<UserBusiness_Product> ubpList=this.ubpServices.findByObjUserAndDownload(objUser,false);
            for(UserBusiness_Product ubp:ubpList){
                productDTOs.add(this.mapper.map(ubp.getObjProduct(), ProductDTO.class));
                //ubp.setDownload(true);
                //this.ubpServices.save(ubp);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        

        return new ResponseEntity<>(productDTOs,HttpStatus.OK);
    }

        /**
     * Actualiza el estado de descarga de productos para un usuario de negocio.
     * 
     * @param userBusinessId El ID del usuario de negocio.
     * @param product_ids    La lista de IDs de productos que se van a marcar como descargados.
     * @return               ResponseEntity con el estado de la operación.
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateDownloadProducts(Long userBusinessId,List<Long> product_ids) {
        try{
            for(Long product_id:product_ids){
                this.ubpServices.updateDownload(product_id, userBusinessId, true);
            }
        }catch(Exception e){
            return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<>(true,HttpStatus.OK);
    }

    /**
     * Retrieves the list of categories associated with the given user business ID.
     *
     * @param  userBusinessId  the ID of the user business
     * @return                 the ResponseEntity containing the list of CategoryDTO objects
     *                         or an INTERNAL_SERVER_ERROR status if an exception occurs
     * @throws EntidadNoExisteException if the UsersBusiness with the given ID does not exist in the database
     */
        @Override
        @Transactional
        public ResponseEntity<?> getCategory(Long userBusinessId) {
            List<CategoryDTO> categoryDTO=new ArrayList<>();
        
            UsersBusiness objUser=this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(objUser==null){
                throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
            }
            try {    
            List<UserBusiness_Category> ubpList=this.ubcServices.findByObjUserAndDownload(objUser,false);
            for(UserBusiness_Category ubp:ubpList){
                categoryDTO.add(this.mapper.map(ubp.getObjCategory(), CategoryDTO.class));
                //ubp.setDownload(true);
                //this.ubpServices.save(ubp);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        

        return new ResponseEntity<>(categoryDTO,HttpStatus.OK);
        }

    /**
     * Retrieves the list of UsersBusinessDTO objects associated with the given userBusinessId.
     *
     * @param  userBusinessId  the ID of the user business
     * @return                 the ResponseEntity containing the list of UsersBusinessDTO objects
     */
        @Override
        @Transactional
        public ResponseEntity<?> getUsersBusiness(Long userBusinessId) {
            List<UsersBusinessDTO> ubpListDTO=new ArrayList<>();
            UsersBusiness objUser=this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(objUser==null){
                throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
            }
            try { 
                if(!objUser.getDownload()){
                    List<UsersBusiness> ubpList=objUser.getBusiness().getUsersBusiness();
                    ubpListDTO= this.mapper.map(ubpList, new TypeToken<List<UsersBusinessDTO>>(){}.getType());
                }   
            
            
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        

        return new ResponseEntity<>(ubpListDTO,HttpStatus.OK);
        }

    /**
     * Updates the download status of a category for a given user business.
     *
     * @param  userBusinessId  the ID of the user business
     * @param  category_ids    the list of category IDs to update
     * @return                 a ResponseEntity indicating the success or failure of the operation
     */
        @Override
        @Transactional
        public ResponseEntity<?> updateDownloadCategory(Long userBusinessId, List<Long> category_ids) {
            try{
                for(Long category_id:category_ids){
                    this.ubcServices.updateDownload(category_id, userBusinessId, true);
                }
            }catch(Exception e){
                return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            return new ResponseEntity<>(true,HttpStatus.OK);
        }

    /**
     * Updates the download status of a user business.
     *
     * @param  userBusinessId  the ID of the user business
     * @return                 a ResponseEntity indicating the success or failure of the operation
     * @throws EntidadNoExisteException  if the user business with the given ID does not exist in the database
     */
        @Override
        @Transactional
        public ResponseEntity<?> updateDownloadUserBusiness(Long userBusinessId) {
            UsersBusiness objUser=this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(objUser==null){
                throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
            }
            try {    
                objUser.setDownload(true);
                this.usersAppDBService.save(objUser);
            
            } catch (Exception e) {
                return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        

        return new ResponseEntity<>(true,HttpStatus.OK);
        }
    
}
