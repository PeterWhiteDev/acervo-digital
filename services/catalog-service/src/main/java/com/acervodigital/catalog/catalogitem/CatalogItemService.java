package com.acervodigital.catalog.catalogitem;

import com.acervodigital.catalog.attribute.AttributeDataType;
import com.acervodigital.catalog.attribute.AttributeDefinition;
import com.acervodigital.catalog.attribute.AttributeDefinitionRepository;
import com.acervodigital.catalog.itemtype.CurrentCollectorProvider;
import com.acervodigital.catalog.itemtype.ItemType;
import com.acervodigital.catalog.itemtype.ItemTypeRepository;
import com.acervodigital.catalog.itemtype.ItemTypeScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CatalogItemService {

    private final CatalogItemRepository catalogItemRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final CurrentCollectorProvider currentCollectorProvider;

    public CatalogItemService(
            CatalogItemRepository catalogItemRepository,
            ItemTypeRepository itemTypeRepository,
            AttributeDefinitionRepository attributeDefinitionRepository,
            CurrentCollectorProvider currentCollectorProvider
    ) {
        this.catalogItemRepository = catalogItemRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.currentCollectorProvider = currentCollectorProvider;
    }

    public Page<CatalogItemResponse> findAll(UUID itemTypeId, Pageable pageable) {
        Page<CatalogItem> items = itemTypeId == null
                ? catalogItemRepository.findAll(pageable)
                : catalogItemRepository.findByItemType_Id(itemTypeId, pageable);
        return items.map(CatalogItemResponse::from);
    }

    public CatalogItemResponse findById(UUID id) {
        CatalogItem item = catalogItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento no encontrado"));
        return CatalogItemResponse.from(item);
    }

    @Transactional
    public CatalogItemResponse create(CreateCatalogItemRequest request) {
        UUID collectorId = currentCollectorProvider.getCurrentCollectorId();
        ItemType itemType = findVisibleItemType(request.itemTypeId(), collectorId);
        Map<String, AttributeDefinition> definitions = findDefinitions(itemType);
        Map<String, String> values = validateAttributeValues(request.attributes(), definitions);

        String description = request.description() == null ? null : request.description().trim();
        if (description != null && description.isEmpty()) {
            description = null;
        }

        CatalogItem item = CatalogItem.create(itemType, request.title().trim(), description, collectorId);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            AttributeValue.create(item, definitions.get(entry.getKey()), entry.getValue());
        }
        return CatalogItemResponse.from(catalogItemRepository.save(item));
    }

    private ItemType findVisibleItemType(UUID itemTypeId, UUID collectorId) {
        return itemTypeRepository.findByIdAndActiveTrue(itemTypeId)
                .filter(type -> type.getScope() == ItemTypeScope.GLOBAL || collectorId.equals(type.getOwnerId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de elemento no encontrado"));
    }

    private Map<String, AttributeDefinition> findDefinitions(ItemType itemType) {
        return attributeDefinitionRepository.findByItemType_IdOrderByDisplayOrderAscNameAsc(itemType.getId())
                .stream()
                .collect(Collectors.toMap(AttributeDefinition::getAttributeKey, Function.identity()));
    }

    private Map<String, String> validateAttributeValues(
            Map<String, String> requestedValues,
            Map<String, AttributeDefinition> definitions
    ) {
        Map<String, String> values = new LinkedHashMap<>();
        if (requestedValues != null) {
            requestedValues.forEach((key, value) -> addValidatedValue(key, value, definitions, values));
        }
        validateRequiredAttributes(definitions, values);
        return values;
    }

    private void addValidatedValue(
            String key,
            String value,
            Map<String, AttributeDefinition> definitions,
            Map<String, String> values
    ) {
        AttributeDefinition definition = definitions.get(key);
        if (definition == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Atributo no definido para este tipo: " + key);
        }
        if (value != null && !value.isBlank()) {
            values.put(key, normalizeValue(definition, value.trim()));
        }
    }

    private void validateRequiredAttributes(
            Map<String, AttributeDefinition> definitions,
            Map<String, String> values
    ) {
        definitions.values().stream()
                .filter(AttributeDefinition::isRequired)
                .filter(definition -> !values.containsKey(definition.getAttributeKey()))
                .findFirst()
                .ifPresent(definition -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Falta el atributo obligatorio: " + definition.getAttributeKey()
                    );
                });
    }

    private String normalizeValue(AttributeDefinition definition, String value) {
        try {
            return switch (definition.getDataType()) {
                case TEXT -> value;
                case INTEGER -> Integer.toString(Integer.parseInt(value));
                case DECIMAL -> new BigDecimal(value).stripTrailingZeros().toPlainString();
                case DATE -> LocalDate.parse(value).toString();
                case BOOLEAN -> normalizeBoolean(value);
            };
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valor incompatible con el tipo del atributo: " + definition.getAttributeKey()
            );
        }
    }

    private String normalizeBoolean(String value) {
        if (value.equalsIgnoreCase("true")) {
            return "true";
        }
        if (value.equalsIgnoreCase("false")) {
            return "false";
        }
        throw new IllegalArgumentException();
    }
}