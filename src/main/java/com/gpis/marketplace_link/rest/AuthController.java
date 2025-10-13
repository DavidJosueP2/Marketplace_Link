package com.gpis.marketplace_link.rest;

import com.gpis.marketplace_link.dto.user.UserResponse;
import com.gpis.marketplace_link.mappers.UserMapper;
import com.gpis.marketplace_link.entities.User;
import com.gpis.marketplace_link.services.EmailVerificationService;
import com.gpis.marketplace_link.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final EmailVerificationService emailVerificationService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam(value = "token", required = false) String token) {
        if (!StringUtils.hasText(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Falta el token de verificación.");
        }
        emailVerificationService.confirm(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam(value = "email", required = false) String email) {
        if (!StringUtils.hasText(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Falta el email.");
        }
        emailVerificationService.resend(email);
        return ResponseEntity.noContent().build();
    }
}
