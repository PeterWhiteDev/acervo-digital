package com.acervodigital.collection.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/collectors")
public class AdminCollectionController {

    private final AdminCollectionService adminCollectionService;

    public AdminCollectionController(AdminCollectionService adminCollectionService) {
        this.adminCollectionService = adminCollectionService;
    }

    @GetMapping("/{collectorId}/collections")
    public AdminCollectorCollectionsResponse findCollections(@PathVariable UUID collectorId) {
        return adminCollectionService.findCollections(collectorId);
    }
}