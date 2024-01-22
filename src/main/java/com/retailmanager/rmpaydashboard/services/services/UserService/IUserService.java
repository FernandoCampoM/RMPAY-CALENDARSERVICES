package com.retailmanager.rmpaydashboard.services.services.UserService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.UserDTO;

public interface IUserService {
    public ResponseEntity<?> save( UserDTO prmUser);
    public ResponseEntity<?> update(Long userId, UserDTO prmUser);
    public boolean delete(Long userId);
    public ResponseEntity<?> findById(Long userId);
    public ResponseEntity<?> findByUsername(String  username);
}
