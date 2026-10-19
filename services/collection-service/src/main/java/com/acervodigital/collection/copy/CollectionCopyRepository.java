package com.acervodigital.collection.copy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionCopyRepository extends JpaRepository<CollectionCopy, UUID> {

    List<CollectionCopy> findByCollection_IdOrderByCreatedAtDesc(UUID collectionId);

    Optional<CollectionCopy> findByIdAndCollection_Id(UUID id, UUID collectionId);

    boolean existsByLocation_Id(UUID locationId);
}