package com.retailmanager.rmpaydashboard.services.services.UsersBusinessService;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.security.AuthCredentials;
import com.retailmanager.rmpaydashboard.services.DTO.EmployeeAuthentication;
import com.retailmanager.rmpaydashboard.services.DTO.EntryExitDTO;
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
    public ResponseEntity<?> getProducts(Long userBusinessId);
    public ResponseEntity<?> getCategory(Long userBusinessId);
    public ResponseEntity<?> getUsersBusiness(Long userBusinessId);
    public ResponseEntity<?> updateDownloadProducts(Long userBusinessId,List<Long> product_ids);
    public ResponseEntity<?> updateDownloadCategory(Long userBusinessId,List<Long> category_ids);
    public ResponseEntity<?> updateDownloadUserBusiness(Long userBusinessId);
    public ResponseEntity<?> registerEntry(String authToken, EmployeeAuthentication prmEmployeeAuthentication);
    public ResponseEntity<?> registerExit(String authToken,EmployeeAuthentication prmEmployeeAuthentication);
    public ResponseEntity<?> getLastActivity(Long prmUserBusinessId);
    public ResponseEntity<?> deleteLastActivity(Long prmUserBusinessId);
    public ResponseEntity<?> updatePonche(Long activityId, EntryExitDTO prmPonche);

    

}
