package com.acervodigital.collection.catalog;

import java.util.UUID;

public interface CatalogClient {

    CatalogItemSummary findItem(UUID itemId);
}