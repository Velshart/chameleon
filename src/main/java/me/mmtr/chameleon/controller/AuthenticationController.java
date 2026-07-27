package me.mmtr.chameleon.controller;

import jakarta.validation.Valid;
import me.mmtr.chameleon.model.dto.AuthenticationRequest;
import me.mmtr.chameleon.model.dto.AuthenticationResponse;
import me.mmtr.chameleon.model.dto.RegisterRequest;
import me.mmtr.chameleon.service.AuthenticationService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<@NonNull AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<@NonNull AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest authenticationRequest
    ) {
        return ResponseEntity.ok(authenticationService.login(authenticationRequest));
    }
}
