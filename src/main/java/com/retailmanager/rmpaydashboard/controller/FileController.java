package com.retailmanager.rmpaydashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.retailmanager.rmpaydashboard.services.services.FileServices.IFileService;

@RestController
@RequestMapping("/api")
@Validated
public class FileController {
    @Autowired
    private IFileService fileService;
    /**
     * Guarda un archivo en el sistema.
     *
     * @param  file	el archivo a guardar
     * @return     	el estado de la operación de guardado
     */
    @PostMapping("/file")
    public ResponseEntity<?> guardarArchivo(@RequestParam("file") MultipartFile file) {
        return fileService.save(file);
    }
}
