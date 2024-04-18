package com.retailmanager.rmpaydashboard.services.services.FileServices;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    public ResponseEntity<?> save(MultipartFile file);

    public ResponseEntity<?> saveImage(MultipartFile file);
    public ResponseEntity<?> downloadImage(String nombreArchivo);
}
