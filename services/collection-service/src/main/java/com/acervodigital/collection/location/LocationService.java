package com.acervodigital.collection.location;

import com.acervodigital.collection.copy.CollectionCopyRepository;
import com.acervodigital.collection.security.CurrentCollectorProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;
    private final CollectionCopyRepository copyRepository;
    private final CurrentCollectorProvider currentCollectorProvider;

    public LocationService(
            LocationRepository locationRepository,
            CollectionCopyRepository copyRepository,
            CurrentCollectorProvider currentCollectorProvider
    ) {
        this.locationRepository = locationRepository;
        this.copyRepository = copyRepository;
        this.currentCollectorProvider = currentCollectorProvider;
    }

    public List<LocationResponse> findOwnedLocations() {
        return locationRepository.findByOwnerIdOrderByNameAsc(currentCollectorProvider.getCurrentCollectorId())
                .stream()
                .map(LocationResponse::from)
                .toList();
    }

    public LocationResponse findOwnedLocation(UUID id) {
        return LocationResponse.from(findOwnedOrAdmin(id));
    }

    @Transactional
    public LocationResponse create(CreateLocationRequest request) {
        Location location = Location.create(
                currentCollectorProvider.getCurrentCollectorId(),
                request.name().trim(),
                normalizeDescription(request.description())
        );
        return LocationResponse.from(locationRepository.save(location));
    }

    @Transactional
    public LocationResponse update(UUID id, UpdateLocationRequest request) {
        Location location = findOwnedOrAdmin(id);
        location.update(request.name().trim(), normalizeDescription(request.description()));
        locationRepository.flush();
        return LocationResponse.from(location);
    }

    @Transactional
    public void delete(UUID id) {
        Location location = findOwnedOrAdmin(id);
        if (copyRepository.existsByLocation_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La ubicacion esta asignada a una o mas copias");
        }
        locationRepository.delete(location);
    }

    private Location findOwnedOrAdmin(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ubicacion no encontrada"));
        if (!location.getOwnerId().equals(currentCollectorProvider.getCurrentCollectorId())
                && !currentCollectorProvider.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede acceder a una ubicacion ajena");
        }
        return location;
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}