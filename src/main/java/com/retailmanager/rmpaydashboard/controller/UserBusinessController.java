package com.retailmanager.rmpaydashboard.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.DTO.UsersBusinessDTO;
import com.retailmanager.rmpaydashboard.services.services.UsersBusinessService.IUsersBusinessService;

@RestController
@RequestMapping("/api")
@Validated
public class UserBusinessController {
    
    @Autowired
    private IUsersBusinessService usersBusinessService;

    /**
     * Save a user business.
     *
     * @param  prmUsersBusiness   the UsersBusinessDTO to be saved
     * @return                   the ResponseEntity representing the result of the save operation
     */
    @PostMapping("/userBusiness")
    public ResponseEntity<?> save(@Valid @RequestBody UsersBusinessDTO prmUsersBusiness) {
        return usersBusinessService.save(prmUsersBusiness);
    }

    /**
     * Updates a user business.
     *
     * @param  userBusinessId  the ID of the user business to be updated
     * @param  prmUsersBusiness  the updated user business data
     * @return         	the ResponseEntity containing the result of the update operation
     */
    @PutMapping("/userBusiness/{userBusinessId}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId,
            @Valid @RequestBody UsersBusinessDTO prmUsersBusiness) {
        return usersBusinessService.update(userBusinessId, prmUsersBusiness);
    }

    /**
     * Deletes a user business by ID.
     *
     * @param  userBusinessId   the ID of the user business to delete
     * @return                  true if the user business was successfully deleted, false otherwise
     */
    @DeleteMapping("/userBusiness/{userBusinessId}")
    public boolean delete(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) {
        return usersBusinessService.delete(userBusinessId);
    }

    /**
     * findById function to find user business by ID.
     *
     * @param  userBusinessId	description of the user business ID parameter
     * @return         	response entity with user business details
     */
    @GetMapping("/userBusiness/{userBusinessId}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) {
        return usersBusinessService.findById(userBusinessId);
    }

    /**
     * A description of the entire Java function.
     *
     * @param  userBusinessId   description of parameter
     * @param  enable           description of parameter
     * @return                  description of return value
     */
    @PutMapping("/userBusiness/{userBusinessId}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId,
            @Valid @PathVariable boolean enable) {
        return usersBusinessService.updateEnable(userBusinessId, enable);
    }
    /**
     * Update the enable status of a permission for a user business.
     *
     * @param  userBusinessId   the ID of the user business
     * @param  idPermission     the ID of the permission
     * @param  enable           the boolean value indicating whether to enable the permission
     * @return                  the ResponseEntity representing the result of the update operation
     */
    @PutMapping("/userBusiness/{userBusinessId}/permission/{idPermission}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId,
           @Valid @PathVariable @Positive(message = "idPermission.positive") Long idPermission, @Valid @PathVariable boolean enable) {
        return usersBusinessService.updatePermission(userBusinessId, idPermission, enable);
    }
    @GetMapping("/userBusiness/permissions")
    public ResponseEntity<?> getAllPermissions() {
        return usersBusinessService.getAllPermissions();
    }
}
