package com.acervodigital.collection.collection;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping("/api/v1/collections")
    public List<CollectionResponse> findOwnedCollections() {
        return collectionService.findOwnedCollections();
    }

    @GetMapping("/api/v1/collections/{id}")
    public CollectionResponse findOwnedCollection(@PathVariable UUID id) {
        return collectionService.findOwnedCollection(id);
    }

    @GetMapping("/api/v1/public/collections")
    public List<CollectionResponse> findPublicCollections() {
        return collectionService.findPublicCollections();
    }

    @GetMapping("/api/v1/public/collections/{id}")
    public CollectionResponse findPublicCollection(@PathVariable UUID id) {
        return collectionService.findPublicCollection(id);
    }

    @PostMapping("/api/v1/collections")
    public ResponseEntity<CollectionResponse> create(@Valid @RequestBody CreateCollectionRequest request) {
        CollectionResponse created = collectionService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/collections/" + created.id())).body(created);
    }

    @PutMapping("/api/v1/collections/{id}")
    public CollectionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCollectionRequest request
    ) {
        return collectionService.update(id, request);
    }

    @DeleteMapping("/api/v1/collections/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        collectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}