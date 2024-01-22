package com.retailmanager.rmpaydashboard.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.UserDTO;
import com.retailmanager.rmpaydashboard.services.services.UserService.IUserService;




@RestController
@RequestMapping("/api")
@Validated
public class UserController {
    @Autowired
    private IUserService userService;
    
    
 
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "userId.positive")Long userId){
        return this.userService.findById(userId);
    }
    @GetMapping("/users")
    public ResponseEntity<?> findByUsername(@Valid @RequestParam(name = "username") @NotBlank(message = "username.notBlank") String username){
        return this.userService.findByUsername(username);
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> save(@Valid @RequestBody UserDTO prmUser){
        return this.userService.save(prmUser);
    }
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "userId.positive")Long userId,@Valid @RequestBody UserDTO prmUser){
        return this.userService.update(userId,prmUser);
    }
    @DeleteMapping("/users/{userId}")
    public Boolean delete(@Valid @PathVariable @Positive(message = "userId.positive")Long userId){
        return this.userService.delete(userId);
    }

}
