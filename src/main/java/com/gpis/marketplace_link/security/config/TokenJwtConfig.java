package com.gpis.marketplace_link.security.config;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Clase de configuración que centraliza las constantes relacionadas
 * con la creación y validación de tokens JWT.
 *
 * Contiene la clave secreta utilizada para firmar los tokens, así como
 * los valores comunes empleados en las cabeceras HTTP y en las respuestas.
 *
*/
public class TokenJwtConfig {

    private TokenJwtConfig() {
    }

    // La clave se toma de la variable de entorno JWT_SECRET.
    // En producción (VPS) DEBE definirse un valor propio de >= 256 bits.
    // El valor por defecto solo sirve para desarrollo local.
    private static final String DEFAULT_SECRET =
            "my-secret-key-for-jwt-token-signing-must-be-at-least-256-bits-long";

    private static String resolveSecret() {
        String envSecret = System.getenv("JWT_SECRET");
        return (envSecret != null && !envSecret.isBlank()) ? envSecret : DEFAULT_SECRET;
    }

    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            resolveSecret().getBytes(StandardCharsets.UTF_8)
    );
    public static final String PREFIX_TOKEN = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "application/json";
}
