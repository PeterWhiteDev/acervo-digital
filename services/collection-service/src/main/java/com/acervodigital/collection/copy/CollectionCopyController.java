package com.acervodigital.collection.copy;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class CollectionCopyController {

    private final CollectionCopyService copyService;

    public CollectionCopyController(CollectionCopyService copyService) {
        this.copyService = copyService;
    }

    @GetMapping("/api/v1/collections/{collectionId}/copies")
    public List<CollectionCopyResponse> findOwnedCopies(@PathVariable UUID collectionId) {
        return copyService.findOwnedCopies(collectionId);
    }

    @GetMapping("/api/v1/collections/{collectionId}/copies/{copyId}")
    public CollectionCopyResponse findOwnedCopy(
            @PathVariable UUID collectionId,
            @PathVariable UUID copyId
    ) {
        return copyService.findOwnedCopy(collectionId, copyId);
    }

    @PostMapping("/api/v1/collections/{collectionId}/copies")
    public ResponseEntity<CollectionCopyResponse> create(
            @PathVariable UUID collectionId,
            @Valid @RequestBody CreateCollectionCopyRequest request
    ) {
        CollectionCopyResponse created = copyService.create(collectionId, request);
        URI location = URI.create("/api/v1/collections/" + collectionId + "/copies/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/api/v1/collections/{collectionId}/copies/{copyId}")
    public CollectionCopyResponse update(
            @PathVariable UUID collectionId,
            @PathVariable UUID copyId,
            @Valid @RequestBody UpdateCollectionCopyRequest request
    ) {
        return copyService.update(collectionId, copyId, request);
    }

    @DeleteMapping("/api/v1/collections/{collectionId}/copies/{copyId}")
    public ResponseEntity<Void> delete(@PathVariable UUID collectionId, @PathVariable UUID copyId) {
        copyService.delete(collectionId, copyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/public/collections/{collectionId}/copies")
    public List<PublicCollectionCopyResponse> findPublicCopies(@PathVariable UUID collectionId) {
        return copyService.findPublicCopies(collectionId);
    }
}