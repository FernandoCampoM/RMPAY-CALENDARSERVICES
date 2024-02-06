package com.retailmanager.rmpaydashboard.repositories;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Sys_general_config;
import com.retailmanager.rmpaydashboard.services.services.EmailService.EmailConfigData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ReqBlackStoneData;


public interface Sys_general_configRepository extends CrudRepository<Sys_general_config,Long>{
    /**
     * Retrieves the Blackstone configuration data from the database.
     *
     * @return         	the Blackstone configuration data
     */
    @Query(value="SELECT " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.AppKey') AS AppKey, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.URL') AS url, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.AppType') AS AppType, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.MID') AS mid, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.CID') AS cid, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.Username') AS Username, " +
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.Password') AS Password;", nativeQuery = true)
    public ReqBlackStoneData getBlackStoneConfig();
    /**
     * Retrieves email configuration data from the database.
     *
     * @return         	email configuration data containing key, emailFrom, emailTo, and emailCCO
     */
    @Query(value="SELECT " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.email.key') AS key, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.email.emailFrom') AS emailFrom, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.email.emailTo') AS emailTo, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.email.emailCCO') AS emailCCO ", nativeQuery = true)
    public EmailConfigData getEmailConfig();
}
