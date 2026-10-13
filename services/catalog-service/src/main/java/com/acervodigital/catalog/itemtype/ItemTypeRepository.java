package com.acervodigital.catalog.itemtype;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemTypeRepository extends JpaRepository<ItemType, UUID> {

    boolean existsByScopeAndOwnerIdAndNameIgnoreCase(ItemTypeScope scope, UUID ownerId, String name);

    List<ItemType> findByScopeAndActiveTrueOrderByNameAsc(ItemTypeScope scope);

    List<ItemType> findByScopeAndOwnerIdAndActiveTrueOrderByNameAsc(ItemTypeScope scope, UUID ownerId);

    Optional<ItemType> findByIdAndActiveTrue(UUID id);
}