package com.acervodigital.identity.admin;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminCollectorController {

    private final AdminCollectorService adminCollectorService;

    public AdminCollectorController(AdminCollectorService adminCollectorService) {
        this.adminCollectorService = adminCollectorService;
    }

    @GetMapping
    public List<AdminCollectorResponse> findAll() {
        return adminCollectorService.findAll();
    }

    @PostMapping
    public ResponseEntity<AdminCollectorResponse> create(@Valid @RequestBody CreateAdminCollectorRequest request) {
        AdminCollectorResponse created = adminCollectorService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/users/" + created.collectorId())).body(created);
    }

    @PutMapping("/{id}")
    public AdminCollectorResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAdminCollectorRequest request
    ) {
        return adminCollectorService.update(UUID.fromString(jwt.getSubject()), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        adminCollectorService.deactivate(UUID.fromString(jwt.getSubject()), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> resetPassword(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody ResetAdminCollectorPasswordRequest request
    ) {
        adminCollectorService.resetPassword(UUID.fromString(jwt.getSubject()), id, request);
        return ResponseEntity.noContent().build();
    }
}