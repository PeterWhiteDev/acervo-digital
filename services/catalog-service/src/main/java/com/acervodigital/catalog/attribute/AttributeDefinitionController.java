package com.acervodigital.catalog.attribute;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/item-types/{itemTypeId}/attributes")
public class AttributeDefinitionController {

    private final AttributeDefinitionService service;

    public AttributeDefinitionController(AttributeDefinitionService service) {
        this.service = service;
    }

    @GetMapping
    public List<AttributeDefinitionResponse> findAll(@PathVariable UUID itemTypeId) {
        return service.findVisibleAttributes(itemTypeId);
    }

    @GetMapping("/{attributeId}")
    public AttributeDefinitionResponse findById(
            @PathVariable UUID itemTypeId,
            @PathVariable UUID attributeId
    ) {
        return service.findVisibleAttribute(itemTypeId, attributeId);
    }

    @PostMapping
    public ResponseEntity<AttributeDefinitionResponse> create(
            @PathVariable UUID itemTypeId,
            @Valid @RequestBody AttributeDefinitionRequest request
    ) {
        AttributeDefinitionResponse created = service.createAttribute(itemTypeId, request);
        URI location = URI.create("/api/v1/item-types/" + itemTypeId + "/attributes/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{attributeId}")
    public AttributeDefinitionResponse update(
            @PathVariable UUID itemTypeId,
            @PathVariable UUID attributeId,
            @Valid @RequestBody AttributeDefinitionRequest request
    ) {
        return service.updateAttribute(itemTypeId, attributeId, request);
    }
}