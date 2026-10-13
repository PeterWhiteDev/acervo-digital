package com.acervodigital.catalog.attribute;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, UUID> {

    List<AttributeDefinition> findByItemType_IdOrderByDisplayOrderAscNameAsc(UUID itemTypeId);

    Optional<AttributeDefinition> findByIdAndItemType_Id(UUID id, UUID itemTypeId);

    boolean existsByItemType_IdAndAttributeKeyIgnoreCase(UUID itemTypeId, String attributeKey);

    boolean existsByItemType_IdAndAttributeKeyIgnoreCaseAndIdNot(UUID itemTypeId, String attributeKey, UUID id);
}