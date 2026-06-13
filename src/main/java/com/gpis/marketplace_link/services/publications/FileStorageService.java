package com.gpis.marketplace_link.services.publications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Servicio de referencias de archivos para publicaciones.
 *
 * NOTA DE DESPLIEGUE (VPS):
 * Este proyecto NO persiste imágenes en disco ni en almacenamiento externo.
 * Antes se usaba Azure Blob Storage; al migrar a VPS se decidió no mantener
 * almacenamiento de binarios. En su lugar, este servicio genera y devuelve un
 * identificador de texto (nombre de archivo) que se guarda como referencia en
 * la base de datos. El contenido binario del archivo no se almacena.
 *
 * Si en el futuro se necesita persistencia real de imágenes, este es el único
 * punto de extensión: implementar aquí el guardado (disco local, S3, etc.).
 */
@Slf4j
@Service
public class FileStorageService {

    /**
     * "Almacena" un archivo devolviendo un identificador de texto único.
     * No escribe el binario en ningún sitio: solo retorna el nombre que se
     * usará como referencia en la base de datos.
     *
     * @param file archivo recibido en la petición
     * @return identificador de texto (UUID + extensión original)
     */
    public String storeFile(MultipartFile file) {
        String originalFileName = file != null ? file.getOriginalFilename() : null;
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString().concat(extension);
        log.info("📝 Referencia de archivo generada (sin persistencia binaria): {}", fileName);
        return fileName;
    }

    /**
     * Elimina la referencia de un archivo.
     * Como no se persiste ningún binario, esta operación es un no-op informativo.
     *
     * @param fileReference referencia almacenada en base de datos
     */
    public void deleteFile(String fileReference) {
        if (fileReference == null || fileReference.isEmpty()) {
            return;
        }
        log.info("🗑️ Referencia de archivo descartada (sin persistencia binaria): {}", fileReference);
    }

    /**
     * Devuelve la URL pública de una referencia de archivo.
     * Si ya es una URL absoluta, la retorna tal cual; si es un nombre relativo,
     * lo expone bajo la ruta /uploads/ para compatibilidad con el frontend.
     *
     * @param fileReference referencia almacenada
     * @return URL utilizable por el frontend, o null si no hay referencia
     */
    public String getFileUrl(String fileReference) {
        if (fileReference == null || fileReference.isEmpty()) {
            return null;
        }
        if (fileReference.startsWith("https://") || fileReference.startsWith("http://")) {
            return fileReference;
        }
        return "/uploads/" + fileReference;
    }
}
