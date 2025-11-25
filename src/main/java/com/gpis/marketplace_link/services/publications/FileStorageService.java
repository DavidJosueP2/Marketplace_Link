package com.gpis.marketplace_link.services.publications;

import com.gpis.marketplace_link.exceptions.business.publications.UploadFolderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("📁 Directorio de uploads local creado: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new UploadFolderException("Error al crear el directorio de subida de archivos", ex);
        }
    }

    /**
     * Almacena un archivo en el sistema de archivos local
     *
     * @param file archivo a almacenar
     * @return nombre del archivo almacenado
     */
    public String storeFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString().concat(extension);

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);
            log.info("📁 Archivo guardado localmente: {}", fileName);
            return fileName;
        } catch (IOException ex) {
            throw new UploadFolderException("No se pudo almacenar el archivo " + fileName, ex);
        }
    }

    /**
     * Obtiene la ruta del archivo
     */
    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName);
    }

    /**
     * Verifica si un archivo existe
     */
    public boolean fileExists(String fileReference) {
        if (fileReference == null || fileReference.isEmpty()) {
            return false;
        }

        Path filePath = getFilePath(fileReference);
        return Files.exists(filePath);
    }

    /**
     * Elimina un archivo
     */
    public void deleteFile(String fileReference) {
        if (fileReference == null || fileReference.isEmpty()) {
            return;
        }

        try {
            Path filePath = getFilePath(fileReference);
            Files.deleteIfExists(filePath);
            log.info("🗑️ Archivo local eliminado: {}", fileReference);
        } catch (IOException ex) {
            throw new UploadFolderException("No se pudo eliminar el archivo " + fileReference, ex);
        }
    }

    /**
     * Obtiene la URL pública del archivo
     */
    public String getFileUrl(String fileReference) {
        if (fileReference == null || fileReference.isEmpty()) {
            return null;
        }

        return "/uploads/" + fileReference;
    }
}
