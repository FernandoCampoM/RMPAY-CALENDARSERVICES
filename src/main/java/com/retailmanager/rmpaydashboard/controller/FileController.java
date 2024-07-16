package com.retailmanager.rmpaydashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    /**
     * Saves an image file to the system.
     *
     * @param  file  the image file to be saved
     * @return       the response entity representing the status of the save operation
     */
    @PostMapping("/file/image")
    public ResponseEntity<?> guardarImagen(@RequestParam("file") MultipartFile file) {
        return fileService.saveImage(file);
    }
    /**
     * Downloads an image file with the given fileId.
     *
     * @param  fileId  the id of the image file to be downloaded
     * @return           the response entity representing the downloaded image file
     */
    @GetMapping("/file/image/{fileId}")
    public ResponseEntity<?> downloadImage(@PathVariable Long fileId) {
        
        return fileService.downloadImage(fileId);
    }
    /**
     * Deletes an image file with the given fileId.
     *
     * @param  fileId   the ID of the image file to be deleted
     * @return          the response entity representing the result of the delete operation
     */
    @DeleteMapping("/file/image/{fileId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long fileId) {
        
        return fileService.deleteImage(fileId);
    }
}
