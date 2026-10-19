package com.acervodigital.catalog.catalogitem;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class PublicCatalogItemController {

    private final CatalogItemService catalogItemService;

    public PublicCatalogItemController(CatalogItemService catalogItemService) {
        this.catalogItemService = catalogItemService;
    }

    @GetMapping("/api/v1/public/items/{id}")
    public PublicCatalogItemResponse findById(@PathVariable UUID id) {
        return PublicCatalogItemResponse.from(catalogItemService.findById(id));
    }
}