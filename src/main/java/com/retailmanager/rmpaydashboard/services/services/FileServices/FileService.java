package com.retailmanager.rmpaydashboard.services.services.FileServices;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.retailmanager.rmpaydashboard.models.FileModel;
import com.retailmanager.rmpaydashboard.repositories.FileRepository;

@Service
public class FileService implements IFileService {
    @Autowired
    private FileRepository fileRepository;
    /**
     * Saves the provided file to the database.
     *
     * @param  file  the file to be saved
     * @return       a ResponseEntity with a message indicating the status of the file save operation
     */
    @Override
    public ResponseEntity<?> save(MultipartFile file) {
        try {
            FileModel archivo = new FileModel();
            archivo.setNombre(file.getOriginalFilename());
            archivo.setExtension(obtenerExtension(file.getOriginalFilename()));
            archivo.setContenido(file.getBytes());
            fileRepository.save(archivo);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("fileId", String.valueOf(archivo.getId()));
            map.put("message", "Archivo guardado correctamente");
            return new ResponseEntity<HashMap<String, String>>(map,HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
            return new  ResponseEntity<String>("Error al guardar el archivo "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private String obtenerExtension(String nombreArchivo) {
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1);
    }
    
}
