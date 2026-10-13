package com.acervodigital.catalog.catalogitem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, UUID> {

    Page<CatalogItem> findByItemType_Id(UUID itemTypeId, Pageable pageable);

    Optional<CatalogItem> findById(UUID id);
}