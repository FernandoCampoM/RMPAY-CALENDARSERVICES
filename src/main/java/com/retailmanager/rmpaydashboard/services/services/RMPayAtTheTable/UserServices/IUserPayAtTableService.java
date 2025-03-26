package com.retailmanager.rmpaydashboard.services.services.RMPayAtTheTable.UserServices;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.RMPayAtTheTable_UserDTO;
import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.UserAuthDTO;

public interface IUserPayAtTableService {
    ResponseEntity<?> createUser(RMPayAtTheTable_UserDTO userDTO);
    ResponseEntity<?> updateUser(Long userId, RMPayAtTheTable_UserDTO userDTO);
    ResponseEntity<?> deleteUser(Long userId);
    ResponseEntity<RMPayAtTheTable_UserDTO> getUserById(Long userId);
    ResponseEntity<?> getAllUsers(Pageable pageable, String filter);
    ResponseEntity<RMPayAtTheTable_UserDTO> getUserByUsername(String username);
    ResponseEntity<RMPayAtTheTable_UserDTO> getUserByMerchantId(String merchantId);
    ResponseEntity<?> passwordChange(Long userId, String newPassword);
    ResponseEntity<?> authenticaton(UserAuthDTO userAuthDTO);
}
