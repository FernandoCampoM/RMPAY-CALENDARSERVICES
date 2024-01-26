package com.retailmanager.rmpaydashboard.services.services.UserService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.UserDTO;

public interface IUserService {
    /**
     * Saves a user.
     * 
     * @param prmUser the user data to be saved
     * @return the response entity for the saved user
     */
    public ResponseEntity<?> save(UserDTO prmUser);
    
    public ResponseEntity<?> update(Long userId, UserDTO prmUser);
    public boolean delete(Long userId);
    public ResponseEntity<?> findById(Long userId);
    public ResponseEntity<?> findByUsername(String  username);
    public ResponseEntity<?> getUserBusiness(Long userId);
    public ResponseEntity<?> updateEnable(Long userId, boolean enable);
}
