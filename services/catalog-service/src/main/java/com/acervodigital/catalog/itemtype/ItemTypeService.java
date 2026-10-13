package com.acervodigital.catalog.itemtype;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ItemTypeService {

    private final ItemTypeRepository repository;
    private final CurrentCollectorProvider currentCollectorProvider;

    public ItemTypeService(ItemTypeRepository repository, CurrentCollectorProvider currentCollectorProvider) {
        this.repository = repository;
        this.currentCollectorProvider = currentCollectorProvider;
    }

    public List<ItemTypeResponse> findVisibleTypes() {
        UUID collectorId = currentCollectorProvider.getCurrentCollectorId();
        List<ItemType> itemTypes = new ArrayList<>(repository.findByScopeAndActiveTrueOrderByNameAsc(ItemTypeScope.GLOBAL));
        itemTypes.addAll(repository.findByScopeAndOwnerIdAndActiveTrueOrderByNameAsc(ItemTypeScope.PERSONAL, collectorId));
        itemTypes.sort(Comparator.comparing(ItemType::getName, String.CASE_INSENSITIVE_ORDER));
        return itemTypes.stream().map(ItemTypeResponse::from).toList();
    }

    public ItemTypeResponse findVisibleType(UUID id) {
        UUID collectorId = currentCollectorProvider.getCurrentCollectorId();
        ItemType itemType = repository.findByIdAndActiveTrue(id)
                .filter(type -> type.getScope() == ItemTypeScope.GLOBAL || collectorId.equals(type.getOwnerId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de elemento no encontrado"));
        return ItemTypeResponse.from(itemType);
    }

    @Transactional
    public ItemTypeResponse createPersonalType(CreateItemTypeRequest request) {
        UUID collectorId = currentCollectorProvider.getCurrentCollectorId();
        String name = request.name().trim();

        if (repository.existsByScopeAndOwnerIdAndNameIgnoreCase(ItemTypeScope.PERSONAL, collectorId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un tipo con ese nombre");
        }

        String description = request.description() == null ? null : request.description().trim();
        if (description != null && description.isEmpty()) {
            description = null;
        }

        ItemType itemType = ItemType.createPersonal(name, description, collectorId);
        return ItemTypeResponse.from(repository.save(itemType));
    }
}