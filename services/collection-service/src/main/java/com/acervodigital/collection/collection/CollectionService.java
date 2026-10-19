package com.acervodigital.collection.collection;

import com.acervodigital.collection.security.CurrentCollectorProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CurrentCollectorProvider currentCollectorProvider;

    public CollectionService(
            CollectionRepository collectionRepository,
            CurrentCollectorProvider currentCollectorProvider
    ) {
        this.collectionRepository = collectionRepository;
        this.currentCollectorProvider = currentCollectorProvider;
    }

    public List<CollectionResponse> findOwnedCollections() {
        return collectionRepository.findByOwnerIdOrderByCreatedAtDesc(currentCollectorProvider.getCurrentCollectorId())
                .stream()
                .map(CollectionResponse::from)
                .toList();
    }

    public List<CollectionResponse> findPublicCollections() {
        return collectionRepository.findByVisibilityOrderByCreatedAtDesc(CollectionVisibility.PUBLIC)
                .stream()
                .map(CollectionResponse::from)
                .toList();
    }

    public CollectionResponse findOwnedCollection(UUID id) {
        return CollectionResponse.from(findOwnedOrAdmin(id));
    }

    public CollectionResponse findPublicCollection(UUID id) {
        CollectionResource collection = collectionRepository.findByIdAndVisibility(id, CollectionVisibility.PUBLIC)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Colección pública no encontrada"));
        return CollectionResponse.from(collection);
    }

    @Transactional
    public CollectionResponse create(CreateCollectionRequest request) {
        CollectionResource collection = CollectionResource.create(
                currentCollectorProvider.getCurrentCollectorId(),
                request.name().trim(),
                normalizeDescription(request.description()),
                request.visibility()
        );
        return CollectionResponse.from(collectionRepository.save(collection));
    }

    @Transactional
    public CollectionResponse update(UUID id, UpdateCollectionRequest request) {
        CollectionResource collection = findOwnedOrAdmin(id);
        collection.update(
                request.name().trim(),
                normalizeDescription(request.description()),
                request.visibility()
        );
        return CollectionResponse.from(collection);
    }

    @Transactional
    public void delete(UUID id) {
        collectionRepository.delete(findOwnedOrAdmin(id));
    }

    private CollectionResource findOwnedOrAdmin(UUID id) {
        CollectionResource collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Colección no encontrada"));
        UUID collectorId = currentCollectorProvider.getCurrentCollectorId();
        if (!collectorId.equals(collection.getOwnerId()) && !currentCollectorProvider.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede modificar una colección ajena");
        }
        return collection;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}