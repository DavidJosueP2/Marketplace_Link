package com.gpis.marketplace_link.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank @Size(min = 6, max = 40) String currentPassword,
        @NotBlank @Size(min = 6, max = 40) String newPassword,
        @NotBlank String confirmNewPassword
) {}
