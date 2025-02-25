package com.retailmanager.rmpaydashboard.controller.RMPayAtTheTable;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.RMPayAtTheTable_UserDTO;
import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.UserAuthDTO;
import com.retailmanager.rmpaydashboard.services.services.RMPayAtTheTable.UserServices.IUserPayAtTableService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/payAtTheTable/users")
@Validated
public class UserPayAtTableController {
    @Autowired
    private IUserPayAtTableService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody RMPayAtTheTable_UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@Valid @PathVariable Long userId, @RequestBody RMPayAtTheTable_UserDTO userDTO) {
        return userService.updateUser(userId, userDTO);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@Valid @PathVariable Long userId) {
        return userService.deleteUser(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<RMPayAtTheTable_UserDTO> getUserById(@Valid @PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<List<RMPayAtTheTable_UserDTO>> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<RMPayAtTheTable_UserDTO> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<RMPayAtTheTable_UserDTO> getUserByMerchantId(@PathVariable String merchantId) {
        return userService.getUserByMerchantId(merchantId);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticaton(@Valid @RequestBody UserAuthDTO userAuthDTO) {
        return userService.authenticaton(userAuthDTO);
    }
}