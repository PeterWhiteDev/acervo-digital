package com.acervodigital.collection.admin;

import com.acervodigital.collection.collection.CollectionRepository;
import com.acervodigital.collection.collection.CollectionResource;
import com.acervodigital.collection.copy.CollectionCopyRepository;
import com.acervodigital.collection.security.CurrentCollectorProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminCollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionCopyRepository copyRepository;
    private final CurrentCollectorProvider currentCollectorProvider;

    public AdminCollectionService(
            CollectionRepository collectionRepository,
            CollectionCopyRepository copyRepository,
            CurrentCollectorProvider currentCollectorProvider
    ) {
        this.collectionRepository = collectionRepository;
        this.copyRepository = copyRepository;
        this.currentCollectorProvider = currentCollectorProvider;
    }

    public AdminCollectorCollectionsResponse findCollections(UUID collectorId) {
        if (!currentCollectorProvider.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el rol ADMIN");
        }
        List<CollectionResource> collections = collectionRepository
                .findByOwnerIdOrderByCreatedAtDesc(collectorId);
        List<AdminCollectionSummary> summaries = collections.stream()
                .map(this::summarize)
                .toList();
        return new AdminCollectorCollectionsResponse(collectorId, summaries.size(), summaries);
    }

    private AdminCollectionSummary summarize(CollectionResource collection) {
        var copies = copyRepository.findByCollection_IdOrderByCreatedAtDesc(collection.getId());
        Map<UUID, LocationItemCount> locations = new TreeMap<>();
        long unassigned = 0;
        for (var copy : copies) {
            if (copy.getLocation() == null) {
                unassigned++;
                continue;
            }
            UUID locationId = copy.getLocation().getId();
            LocationItemCount previous = locations.get(locationId);
            locations.put(locationId, previous == null
                    ? new LocationItemCount(locationId, copy.getLocation().getName(), 1)
                    : new LocationItemCount(locationId, previous.locationName(), previous.itemCount() + 1));
        }
        return new AdminCollectionSummary(
                collection.getId(),
                collection.getName(),
                collection.getVisibility(),
                copies.size(),
                List.copyOf(locations.values()),
                unassigned
        );
    }
}