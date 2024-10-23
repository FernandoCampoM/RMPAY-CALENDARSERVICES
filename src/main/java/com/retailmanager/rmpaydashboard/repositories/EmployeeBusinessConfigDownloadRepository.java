package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.BusinessConfiguration;
import com.retailmanager.rmpaydashboard.models.EmployeeBusinessConfigDownload;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;

public interface EmployeeBusinessConfigDownloadRepository extends CrudRepository<EmployeeBusinessConfigDownload, Long> {
    public List<EmployeeBusinessConfigDownload> findByObjUser(UsersBusiness objUser);
    public List<EmployeeBusinessConfigDownload> findByObjUserAndDownload(UsersBusiness objUser, boolean download);
    public List<EmployeeBusinessConfigDownload>  findByObjConfiguration(BusinessConfiguration objConfiguration);
    @Modifying
    @Query("UPDATE EmployeeBusinessConfigDownload u SET u.download = :download WHERE u.objConfiguration.Configurationid = :Configurationid and u.objUser.userBusinessId = :user_id")
    public void updateDownload(Long Configurationid, Long user_id,boolean download);

    @Modifying
    @Query("DELETE FROM EmployeeBusinessConfigDownload u WHERE u.objConfiguration.Configurationid = :Configurationid")
    public void deleteByConfigurationId(Long Configurationid);
}
