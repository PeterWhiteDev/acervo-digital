package com.acervodigital.collection.wishlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistEntryRepository extends JpaRepository<WishlistEntry, UUID> {

    List<WishlistEntry> findByCollection_IdOrderByCreatedAtDesc(UUID collectionId);

    Optional<WishlistEntry> findByIdAndCollection_Id(UUID id, UUID collectionId);
}