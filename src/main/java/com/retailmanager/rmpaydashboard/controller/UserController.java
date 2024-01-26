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
    
    
 
    /**
     * A method to find a user by their ID.
     *
     * @param  userId   the ID of the user to find
     * @return          the ResponseEntity containing the user found by ID
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "userId.positive")Long userId){
        return this.userService.findById(userId);
    }
    /**
     * Finds a user by username.
     *
     * @param  username	description of the username parameter
     * @return         	description of the ResponseEntity return value
     */
    @GetMapping("/users")
    public ResponseEntity<?> findByUsername(@Valid @RequestParam(name = "username") @NotBlank(message = "username.notBlank") String username){
        return this.userService.findByUsername(username);
    }
    
    /**
     * Save a user.
     *
     * @param  prmUser  the user to be saved
     * @return          the response entity
     */
    @PostMapping("/users")
    public ResponseEntity<?> save(@Valid @RequestBody UserDTO prmUser){
        return this.userService.save(prmUser);
    }
    /**
     * Updates a user by userId.
     *
     * @param  userId   the ID of the user to update
     * @param  prmUser  the updated user information
     * @return          the ResponseEntity containing the updated user
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "userId.positive")Long userId,@Valid @RequestBody UserDTO prmUser){
        return this.userService.update(userId,prmUser);
    }
    /**
     * Delete user by ID.
     *
     * @param  userId	ID of the user to be deleted
     * @return        	true if the user is successfully deleted, false otherwise
     */
    @DeleteMapping("/users/{userId}")
    public Boolean delete(@Valid @PathVariable @Positive(message = "userId.positive")Long userId){
        return this.userService.delete(userId);
    }

    /**
     * Retrieves the business information for a specific user.
     *
     * @param  userId   the ID of the user
     * @return          the ResponseEntity containing the user's business information
     */
    @GetMapping("/users/{userId}/business")
    public ResponseEntity<?> getUserBusiness(@Valid @PathVariable @Positive(message = "userId.positive")Long userId){
        return this.userService.getUserBusiness(userId);
    }

}
