package com.retailmanager.rmpaydashboard.services.services.UsersApp;

import org.springframework.http.ResponseEntity;

public interface IUserApp {
    public ResponseEntity<?> updateEnable(Long userBusinessId, boolean enable);
}
