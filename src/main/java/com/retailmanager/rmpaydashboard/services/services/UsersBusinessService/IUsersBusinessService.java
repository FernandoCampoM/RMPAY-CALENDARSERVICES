package com.retailmanager.rmpaydashboard.services.services.UsersBusinessService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.UsersBusinessDTO;

public interface IUsersBusinessService {
    public ResponseEntity<?> save(UsersBusinessDTO prmUsersBusiness);
    public ResponseEntity<?> update(Long userBusinessId, UsersBusinessDTO prmUsersBusiness);
    public boolean delete(Long userBusinessId);
    public ResponseEntity<?> findById(Long userBusinessId);
    public ResponseEntity<?> updateEnable(Long userBusinessId, boolean enable);
    public ResponseEntity<?> findByBusiness(Long idBusiness);
    public ResponseEntity<?> updatePermission(Long idUser, Long idPermission, boolean enable);
    public ResponseEntity<?> getAllPermissions();

}
