package com.acervodigital.collection.wishlist;

import com.acervodigital.collection.copy.CollectionCopyResponse;
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
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/api/v1/collections/{collectionId}/wishlist")
    public List<WishlistEntryResponse> findEntries(@PathVariable UUID collectionId) {
        return wishlistService.findOwnedEntries(collectionId);
    }

    @GetMapping("/api/v1/collections/{collectionId}/wishlist/{entryId}")
    public WishlistEntryResponse findEntry(
            @PathVariable UUID collectionId,
            @PathVariable UUID entryId
    ) {
        return wishlistService.findOwnedEntry(collectionId, entryId);
    }

    @PostMapping("/api/v1/collections/{collectionId}/wishlist")
    public ResponseEntity<WishlistEntryResponse> create(
            @PathVariable UUID collectionId,
            @Valid @RequestBody WishlistEntryRequest request
    ) {
        WishlistEntryResponse created = wishlistService.create(collectionId, request);
        URI location = URI.create("/api/v1/collections/" + collectionId + "/wishlist/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/api/v1/collections/{collectionId}/wishlist/{entryId}")
    public WishlistEntryResponse update(
            @PathVariable UUID collectionId,
            @PathVariable UUID entryId,
            @Valid @RequestBody WishlistEntryRequest request
    ) {
        return wishlistService.update(collectionId, entryId, request);
    }

    @DeleteMapping("/api/v1/collections/{collectionId}/wishlist/{entryId}")
    public ResponseEntity<Void> delete(@PathVariable UUID collectionId, @PathVariable UUID entryId) {
        wishlistService.delete(collectionId, entryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/collections/{collectionId}/wishlist/{entryId}/convert")
    public ResponseEntity<CollectionCopyResponse> convert(
            @PathVariable UUID collectionId,
            @PathVariable UUID entryId,
            @Valid @RequestBody ConvertWishlistEntryRequest request
    ) {
        CollectionCopyResponse copy = wishlistService.convert(collectionId, entryId, request);
        URI location = URI.create("/api/v1/collections/" + collectionId + "/copies/" + copy.id());
        return ResponseEntity.created(location).body(copy);
    }
}