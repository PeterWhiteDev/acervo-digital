package com.acervodigital.catalog.attribute;

import com.acervodigital.catalog.itemtype.CurrentCollectorProvider;
import com.acervodigital.catalog.itemtype.ItemType;
import com.acervodigital.catalog.itemtype.ItemTypeRepository;
import com.acervodigital.catalog.itemtype.ItemTypeScope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AttributeDefinitionService {

    private final AttributeDefinitionRepository attributeRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final CurrentCollectorProvider currentCollectorProvider;

    public AttributeDefinitionService(
            AttributeDefinitionRepository attributeRepository,
            ItemTypeRepository itemTypeRepository,
            CurrentCollectorProvider currentCollectorProvider
    ) {
        this.attributeRepository = attributeRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.currentCollectorProvider = currentCollectorProvider;
    }

    public List<AttributeDefinitionResponse> findVisibleAttributes(UUID itemTypeId) {
        findVisibleItemType(itemTypeId);
        return attributeRepository.findByItemType_IdOrderByDisplayOrderAscNameAsc(itemTypeId)
                .stream()
                .map(AttributeDefinitionResponse::from)
                .toList();
    }

    public AttributeDefinitionResponse findVisibleAttribute(UUID itemTypeId, UUID attributeId) {
        findVisibleItemType(itemTypeId);
        AttributeDefinition definition = attributeRepository.findByIdAndItemType_Id(attributeId, itemTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atributo no encontrado"));
        return AttributeDefinitionResponse.from(definition);
    }

    @Transactional
    public AttributeDefinitionResponse createAttribute(UUID itemTypeId, AttributeDefinitionRequest request) {
        ItemType itemType = findEditableItemType(itemTypeId);
        String attributeKey = request.attributeKey();

        if (attributeRepository.existsByItemType_IdAndAttributeKeyIgnoreCase(itemTypeId, attributeKey)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un atributo con esa clave en el tipo");
        }

        AttributeDefinition definition = AttributeDefinition.create(
                itemType,
                request.name().trim(),
                attributeKey,
                request.dataType(),
                request.required(),
                request.displayOrder()
        );
        return AttributeDefinitionResponse.from(attributeRepository.save(definition));
    }

    @Transactional
    public AttributeDefinitionResponse updateAttribute(
            UUID itemTypeId,
            UUID attributeId,
            AttributeDefinitionRequest request
    ) {
        findEditableItemType(itemTypeId);
        AttributeDefinition definition = attributeRepository.findByIdAndItemType_Id(attributeId, itemTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atributo no encontrado"));
        String attributeKey = request.attributeKey();

        if (attributeRepository.existsByItemType_IdAndAttributeKeyIgnoreCaseAndIdNot(
                itemTypeId,
                attributeKey,
                attributeId
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un atributo con esa clave en el tipo");
        }

        definition.update(
                request.name().trim(),
                attributeKey,
                request.dataType(),
                request.required(),
                request.displayOrder()
        );
        return AttributeDefinitionResponse.from(definition);
    }

    private ItemType findVisibleItemType(UUID itemTypeId) {
        UUID collectorId = currentCollectorProvider.getCurrentCollectorId();
        return itemTypeRepository.findByIdAndActiveTrue(itemTypeId)
                .filter(itemType -> itemType.getScope() == ItemTypeScope.GLOBAL
                        || collectorId.equals(itemType.getOwnerId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de elemento no encontrado"));
    }

    private ItemType findEditableItemType(UUID itemTypeId) {
        ItemType itemType = findVisibleItemType(itemTypeId);
        if (itemType.getScope() == ItemTypeScope.GLOBAL && !currentCollectorProvider.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Las plantillas globales no se pueden modificar");
        }
        return itemType;
    }
}