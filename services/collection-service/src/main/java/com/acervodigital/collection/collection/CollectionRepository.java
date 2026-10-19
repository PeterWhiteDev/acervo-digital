package com.acervodigital.collection.collection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<CollectionResource, UUID> {

	List<CollectionResource> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

	List<CollectionResource> findByVisibilityOrderByCreatedAtDesc(CollectionVisibility visibility);

	Optional<CollectionResource> findByIdAndVisibility(UUID id, CollectionVisibility visibility);

	Optional<CollectionResource> findByIdAndOwnerId(UUID id, UUID ownerId);
}