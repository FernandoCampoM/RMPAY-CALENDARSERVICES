package com.retailmanager.rmpaydashboard.services.services.EmailService;

public interface EmailConfigProjection {
    String getAppKey();
    String getEmailFrom();
    String getEmailTo();
    String getEmailCCO();
}