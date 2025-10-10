package com.gpis.marketplace_link.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @NotBlank(message = "No se ha enviado el token")
        String tokenValue,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 40, message = "La contraseña debe tener entre 6 y 40 caracteres")
        String newPassword
) {


}
