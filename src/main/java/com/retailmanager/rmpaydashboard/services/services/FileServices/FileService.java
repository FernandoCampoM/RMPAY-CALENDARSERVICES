package com.retailmanager.rmpaydashboard.services.services.FileServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.retailmanager.rmpaydashboard.models.FileModel;
import com.retailmanager.rmpaydashboard.repositories.FileRepository;

@Service
public class FileService implements IFileService {
    
    String rutaProyecto = System.getProperty("user.dir");
    String directorioImagenes = rutaProyecto + "/src/main/resources/static/images/";

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
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("message", "Error al guardar el archivo "+e.getMessage());
            return new  ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Returns the file extension of the given file name.
     *
     * @param  nombreArchivo   the name of the file
     * @return                 the file extension
     */
    private String obtenerExtension(String nombreArchivo) {
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1);
    }
    /**
     * Saves an image file to the system.
     *
     * @param  file  the image file to be saved
     * @return       the response entity representing the status of the save operation
     */
    @Override
    public ResponseEntity<?> saveImage(MultipartFile file) {

        if (file.isEmpty()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("message", "El archivo está vacío");
            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
        }

        try {
            FileModel archivo = new FileModel();
            archivo.setNombre(generateSafeFileName(file.getOriginalFilename()));
            archivo.setExtension(obtenerExtension(file.getOriginalFilename()));
            archivo.setContenido(file.getBytes());
            

            File objFile = new File(directorioImagenes + archivo.getNombre());
            FileOutputStream fos = new FileOutputStream(objFile);
            fos.write(archivo.getContenido());
            fos.close();


            HashMap<String, String> map = new HashMap<String, String>();
            map.put("fileName", archivo.getNombre());
            map.put("message", "Archivo guardado correctamente");
            return new ResponseEntity<HashMap<String, String>>(map,HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("message", "Error al guardar el archivo "+e.getMessage());
            return new  ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * A method to download an image based on the provided file name.
     *
     * @param  nombreArchivo    the name of the image file to be downloaded
     * @return                  a ResponseEntity containing the image data or error message
     */
    @Override
    public ResponseEntity<?> downloadImage(String nombreArchivo) {
        try{        
            String format = nombreArchivo.substring(nombreArchivo.indexOf(".")+1,nombreArchivo.length());
            System.out.println("Path: " + directorioImagenes+ nombreArchivo);
            File file = new File(directorioImagenes+ nombreArchivo);

            if(!file.exists()){
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("message", "El archivo no existe");
                return new ResponseEntity<>(map,HttpStatus.NOT_FOUND);
            }
            ResponseEntity<?> response = ResponseEntity.ok().headers(headers -> {
                headers.set("content-type", "image/" + format);
                headers.setContentLength(file.length());
            }).body(fileToByteArray(file));
            

            return response;
            
        }catch(NullPointerException e){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("message", e.getMessage());
            return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(Exception ex){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("message", ex.getMessage());
            return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Converts a file to a byte array.
     *
     * @param  file  the file to be converted
     * @return       a byte array containing the contents of the file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static byte[] fileToByteArray(File file) throws IOException {
        // Crea un flujo de entrada para leer el archivo
        FileInputStream fis = new FileInputStream(file);

        // Crea un arreglo de bytes para almacenar los datos del archivo
        byte[] bytesArray = new byte[(int) file.length()];

        // Lee los datos del archivo en el arreglo de bytes
        fis.read(bytesArray);

        // Cierra el flujo de entrada
        fis.close();

        return bytesArray;
    }
    /**
     * Normalizar el nombre de archivo para eliminar caracteres especiales o no ASCII
     *
     * @param  originalFileName  el nombre de archivo original
     * @return                  el nombre de archivo normalizado
     */
    public String generateSafeFileName(String originalFileName) {
        // Normalizar el nombre de archivo para eliminar caracteres especiales o no ASCII
        String normalizedFileName = Normalizer.normalize(originalFileName, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        
        // Eliminar caracteres no permitidos en nombres de archivos
        normalizedFileName = normalizedFileName.replaceAll("[/\\\\:*?\"<>| ]", "_");
        
        // Limitar la longitud del nombre de archivo si es necesario
        int maxLength = 255; // Longitud máxima permitida por la mayoría de los sistemas de archivos
        if (normalizedFileName.length() > maxLength) {
            normalizedFileName = normalizedFileName.substring(0, maxLength);
        }
        
        return normalizedFileName;
    }
}
