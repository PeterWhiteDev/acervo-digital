package com.acervodigital.catalog.catalogitem;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items")
public class CatalogItemController {

    private final CatalogItemService service;

    public CatalogItemController(CatalogItemService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CatalogItemResponse> findAll(
            @RequestParam(required = false) UUID itemTypeId,
            @PageableDefault(size = 20, sort = "title") Pageable pageable
    ) {
        return service.findAll(itemTypeId, pageable);
    }

    @GetMapping("/{id}")
    public CatalogItemResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<CatalogItemResponse> create(@Valid @RequestBody CreateCatalogItemRequest request) {
        CatalogItemResponse created = service.create(request);
        URI location = URI.create("/api/v1/items/" + created.id());
        return ResponseEntity.created(location).body(created);
    }
}