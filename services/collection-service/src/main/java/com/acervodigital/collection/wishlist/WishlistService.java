package com.acervodigital.collection.wishlist;

import com.acervodigital.collection.catalog.CatalogClient;
import com.acervodigital.collection.catalog.CatalogItemSummary;
import com.acervodigital.collection.collection.CollectionRepository;
import com.acervodigital.collection.collection.CollectionResource;
import com.acervodigital.collection.copy.CollectionCopyResponse;
import com.acervodigital.collection.copy.CollectionCopyService;
import com.acervodigital.collection.copy.CreateCollectionCopyRequest;
import com.acervodigital.collection.security.CurrentCollectorProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WishlistService {

    private final CollectionRepository collectionRepository;
    private final WishlistEntryRepository entryRepository;
    private final CatalogClient catalogClient;
    private final CollectionCopyService copyService;
    private final CurrentCollectorProvider currentCollectorProvider;

    public WishlistService(
            CollectionRepository collectionRepository,
            WishlistEntryRepository entryRepository,
            CatalogClient catalogClient,
            CollectionCopyService copyService,
            CurrentCollectorProvider currentCollectorProvider
    ) {
        this.collectionRepository = collectionRepository;
        this.entryRepository = entryRepository;
        this.catalogClient = catalogClient;
        this.copyService = copyService;
        this.currentCollectorProvider = currentCollectorProvider;
    }

    public List<WishlistEntryResponse> findOwnedEntries(UUID collectionId) {
        CollectionResource collection = findOwnedCollection(collectionId);
        return entryRepository.findByCollection_IdOrderByCreatedAtDesc(collection.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WishlistEntryResponse findOwnedEntry(UUID collectionId, UUID entryId) {
        findOwnedCollection(collectionId);
        return toResponse(findEntry(collectionId, entryId));
    }

    @Transactional
    public WishlistEntryResponse create(UUID collectionId, WishlistEntryRequest request) {
        CollectionResource collection = findOwnedCollection(collectionId);
        CatalogItemSummary catalogItem = catalogClient.findItem(request.catalogItemId());
        WishlistEntry entry = WishlistEntry.create(
                collection,
                catalogItem.id(),
                request.priority(),
                normalizeNotes(request.notes())
        );
        return WishlistEntryResponse.from(entryRepository.save(entry), catalogItem.title());
    }

    @Transactional
    public WishlistEntryResponse update(UUID collectionId, UUID entryId, WishlistEntryRequest request) {
        findOwnedCollection(collectionId);
        WishlistEntry entry = findEntry(collectionId, entryId);
        CatalogItemSummary catalogItem = catalogClient.findItem(request.catalogItemId());
        entry.update(catalogItem.id(), request.priority(), normalizeNotes(request.notes()));
        return WishlistEntryResponse.from(entry, catalogItem.title());
    }

    @Transactional
    public void delete(UUID collectionId, UUID entryId) {
        findOwnedCollection(collectionId);
        entryRepository.delete(findEntry(collectionId, entryId));
    }

    @Transactional
    public CollectionCopyResponse convert(
            UUID collectionId,
            UUID entryId,
            ConvertWishlistEntryRequest request
    ) {
        findOwnedCollection(collectionId);
        WishlistEntry entry = findEntry(collectionId, entryId);
        String notes = request.notes() == null ? entry.getNotes() : normalizeNotes(request.notes());
        CollectionCopyResponse copy = copyService.create(collectionId, new CreateCollectionCopyRequest(
                entry.getCatalogItemId(),
                request.condition(),
                request.locationId(),
                request.acquiredOn(),
                request.purchasePrice(),
                request.currentValue(),
                notes
        ));
        entryRepository.delete(entry);
        return copy;
    }

    private WishlistEntryResponse toResponse(WishlistEntry entry) {
        return WishlistEntryResponse.from(entry, catalogClient.findItem(entry.getCatalogItemId()).title());
    }

    private CollectionResource findOwnedCollection(UUID collectionId) {
        CollectionResource collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleccion no encontrada"));
        UUID collectorId = currentCollectorProvider.getCurrentCollectorId();
        if (!collectorId.equals(collection.getOwnerId()) && !currentCollectorProvider.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede acceder a una coleccion ajena");
        }
        return collection;
    }

    private WishlistEntry findEntry(UUID collectionId, UUID entryId) {
        return entryRepository.findByIdAndCollection_Id(entryId, collectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deseo no encontrado"));
    }

    private String normalizeNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
    }
}