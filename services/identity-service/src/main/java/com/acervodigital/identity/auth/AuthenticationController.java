package com.acervodigital.identity.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<CollectorResponse> register(@Valid @RequestBody RegisterCollectorRequest request) {
        CollectorResponse collector = authenticationService.register(request);
        return ResponseEntity.status(201).body(collector);
    }

    @PostMapping("/token")
    public TokenResponse createToken(@Valid @RequestBody CreateTokenRequest request) {
        return authenticationService.createToken(request);
    }
}