package com.acervodigital.catalog.itemtype;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/item-types")
public class ItemTypeController {

    private final ItemTypeService service;

    public ItemTypeController(ItemTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemTypeResponse> findAll() {
        return service.findVisibleTypes();
    }

    @GetMapping("/{id}")
    public ItemTypeResponse findById(@PathVariable UUID id) {
        return service.findVisibleType(id);
    }

    @PostMapping
    public ResponseEntity<ItemTypeResponse> create(@Valid @RequestBody CreateItemTypeRequest request) {
        ItemTypeResponse created = service.createPersonalType(request);
        URI location = URI.create("/api/v1/item-types/" + created.id());
        return ResponseEntity.created(location).body(created);
    }
}