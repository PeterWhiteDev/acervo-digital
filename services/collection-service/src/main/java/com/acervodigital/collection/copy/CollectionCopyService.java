package com.acervodigital.collection.copy;

import com.acervodigital.collection.catalog.CatalogClient;
import com.acervodigital.collection.catalog.CatalogItemSummary;
import com.acervodigital.collection.collection.CollectionRepository;
import com.acervodigital.collection.collection.CollectionResource;
import com.acervodigital.collection.collection.CollectionVisibility;
import com.acervodigital.collection.location.Location;
import com.acervodigital.collection.location.LocationRepository;
import com.acervodigital.collection.security.CurrentCollectorProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CollectionCopyService {

    private final CollectionRepository collectionRepository;
    private final CollectionCopyRepository copyRepository;
    private final LocationRepository locationRepository;
    private final CatalogClient catalogClient;
    private final CurrentCollectorProvider currentCollectorProvider;

    public CollectionCopyService(
            CollectionRepository collectionRepository,
            CollectionCopyRepository copyRepository,
            LocationRepository locationRepository,
            CatalogClient catalogClient,
            CurrentCollectorProvider currentCollectorProvider
    ) {
        this.collectionRepository = collectionRepository;
        this.copyRepository = copyRepository;
        this.locationRepository = locationRepository;
        this.catalogClient = catalogClient;
        this.currentCollectorProvider = currentCollectorProvider;
    }

    public List<CollectionCopyResponse> findOwnedCopies(UUID collectionId) {
        CollectionResource collection = findOwnedCollection(collectionId);
        return copyRepository.findByCollection_IdOrderByCreatedAtDesc(collection.getId())
                .stream()
                .map(this::toPrivateResponse)
                .toList();
    }

    public CollectionCopyResponse findOwnedCopy(UUID collectionId, UUID copyId) {
        findOwnedCollection(collectionId);
        return toPrivateResponse(findCopy(collectionId, copyId));
    }

    @Transactional
    public CollectionCopyResponse create(UUID collectionId, CreateCollectionCopyRequest request) {
        CollectionResource collection = findOwnedCollection(collectionId);
        CatalogItemSummary catalogItem = catalogClient.findItem(request.catalogItemId());
        Location location = findOwnedLocation(request.locationId(), collection.getOwnerId());
        CollectionCopy copy = CollectionCopy.create(
                collection,
                catalogItem.id(),
                request.condition(),
                location,
                request.acquiredOn(),
                request.purchasePrice(),
                request.currentValue(),
                normalizeNotes(request.notes())
        );
        return CollectionCopyResponse.from(copyRepository.save(copy), catalogItem.title());
    }

    @Transactional
    public CollectionCopyResponse update(UUID collectionId, UUID copyId, UpdateCollectionCopyRequest request) {
        CollectionResource collection = findOwnedCollection(collectionId);
        CollectionCopy copy = findCopy(collectionId, copyId);
        CatalogItemSummary catalogItem = catalogClient.findItem(request.catalogItemId());
        Location location = findOwnedLocation(request.locationId(), collection.getOwnerId());
        copy.update(
                catalogItem.id(),
                request.condition(),
                location,
                request.acquiredOn(),
                request.purchasePrice(),
                request.currentValue(),
                normalizeNotes(request.notes())
        );
        return CollectionCopyResponse.from(copy, catalogItem.title());
    }

    @Transactional
    public void delete(UUID collectionId, UUID copyId) {
        findOwnedCollection(collectionId);
        copyRepository.delete(findCopy(collectionId, copyId));
    }

    public List<PublicCollectionCopyResponse> findPublicCopies(UUID collectionId) {
        CollectionResource collection = collectionRepository.findByIdAndVisibility(
                        collectionId,
                        CollectionVisibility.PUBLIC
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Coleccion publica no encontrada"));
        return copyRepository.findByCollection_IdOrderByCreatedAtDesc(collection.getId())
                .stream()
                .map(copy -> PublicCollectionCopyResponse.from(
                        copy,
                        catalogClient.findItem(copy.getCatalogItemId()).title()
                ))
                .toList();
    }

    private CollectionCopyResponse toPrivateResponse(CollectionCopy copy) {
        return CollectionCopyResponse.from(copy, catalogClient.findItem(copy.getCatalogItemId()).title());
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

    private CollectionCopy findCopy(UUID collectionId, UUID copyId) {
        return copyRepository.findByIdAndCollection_Id(copyId, collectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Copia no encontrada"));
    }

    private Location findOwnedLocation(UUID locationId, UUID collectionOwnerId) {
        if (locationId == null) {
            return null;
        }
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ubicacion no encontrada"));
        if (!collectionOwnerId.equals(location.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ubicacion no encontrada");
        }
        return location;
    }

    private String normalizeNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
    }
}