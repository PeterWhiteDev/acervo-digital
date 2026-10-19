import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AttributeDataType,
  AttributeDefinition,
  CatalogIdentifier,
  CatalogItem,
  CatalogItemPage,
  CollectionCopy,
  CollectionResource,
  CollectionVisibility,
  CopyCondition,
  ItemType,
  Location,
  WishlistEntry,
  WishlistPriority,
} from './models';

@Injectable({ providedIn: 'root' })
export class CatalogApi {
  private readonly http = inject(HttpClient);

  listTypes(): Observable<ItemType[]> {
    return this.http.get<ItemType[]>('/api/v1/item-types');
  }

  createType(request: { name: string; description?: string | null }): Observable<ItemType> {
    return this.http.post<ItemType>('/api/v1/item-types', request);
  }

  listAttributes(itemTypeId: string): Observable<AttributeDefinition[]> {
    return this.http.get<AttributeDefinition[]>(`/api/v1/item-types/${itemTypeId}/attributes`);
  }

  createAttribute(itemTypeId: string, request: {
    name: string;
    attributeKey: string;
    dataType: AttributeDataType;
    required: boolean;
    displayOrder: number;
  }): Observable<AttributeDefinition> {
    return this.http.post<AttributeDefinition>(`/api/v1/item-types/${itemTypeId}/attributes`, request);
  }

  listItems(itemTypeId: string | null, page = 0, size = 40): Observable<CatalogItemPage> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'title,asc');
    if (itemTypeId) {
      params = params.set('itemTypeId', itemTypeId);
    }
    return this.http.get<CatalogItemPage>('/api/v1/items', { params });
  }

  getItem(id: string): Observable<CatalogItem> {
    return this.http.get<CatalogItem>(`/api/v1/items/${id}`);
  }

  createItem(request: {
    itemTypeId: string;
    title: string;
    description?: string | null;
    identifiers?: CatalogIdentifier[];
    attributes?: Record<string, string>;
  }): Observable<CatalogItem> {
    return this.http.post<CatalogItem>('/api/v1/items', request);
  }

  updateItem(id: string, request: {
    title: string;
    description?: string | null;
    identifiers?: CatalogIdentifier[];
    attributes?: Record<string, string>;
  }): Observable<CatalogItem> {
    return this.http.put<CatalogItem>(`/api/v1/items/${id}`, request);
  }
}

@Injectable({ providedIn: 'root' })
export class CollectionApi {
  private readonly http = inject(HttpClient);

  listCollections(): Observable<CollectionResource[]> {
    return this.http.get<CollectionResource[]>('/api/v1/collections');
  }

  createCollection(request: {
    name: string;
    description?: string | null;
    visibility: CollectionVisibility;
  }): Observable<CollectionResource> {
    return this.http.post<CollectionResource>('/api/v1/collections', request);
  }

  updateCollection(id: string, request: {
    name: string;
    description?: string | null;
    visibility: CollectionVisibility;
  }): Observable<CollectionResource> {
    return this.http.put<CollectionResource>(`/api/v1/collections/${id}`, request);
  }

  deleteCollection(id: string): Observable<void> {
    return this.http.delete<void>(`/api/v1/collections/${id}`);
  }

  listLocations(): Observable<Location[]> {
    return this.http.get<Location[]>('/api/v1/locations');
  }

  createLocation(request: { name: string; description?: string | null }): Observable<Location> {
    return this.http.post<Location>('/api/v1/locations', request);
  }

  updateLocation(id: string, request: { name: string; description?: string | null }): Observable<Location> {
    return this.http.put<Location>(`/api/v1/locations/${id}`, request);
  }

  deleteLocation(id: string): Observable<void> {
    return this.http.delete<void>(`/api/v1/locations/${id}`);
  }

  listCopies(collectionId: string): Observable<CollectionCopy[]> {
    return this.http.get<CollectionCopy[]>(`/api/v1/collections/${collectionId}/copies`);
  }

  createCopy(collectionId: string, request: {
    catalogItemId: string;
    condition: CopyCondition;
    locationId?: string | null;
    acquiredOn: string;
    purchasePrice?: number | null;
    currentValue?: number | null;
    notes?: string | null;
  }): Observable<CollectionCopy> {
    return this.http.post<CollectionCopy>(`/api/v1/collections/${collectionId}/copies`, request);
  }

  updateCopy(collectionId: string, copyId: string, request: {
    catalogItemId: string;
    condition: CopyCondition;
    locationId?: string | null;
    acquiredOn: string;
    purchasePrice?: number | null;
    currentValue?: number | null;
    notes?: string | null;
  }): Observable<CollectionCopy> {
    return this.http.put<CollectionCopy>(`/api/v1/collections/${collectionId}/copies/${copyId}`, request);
  }

  deleteCopy(collectionId: string, copyId: string): Observable<void> {
    return this.http.delete<void>(`/api/v1/collections/${collectionId}/copies/${copyId}`);
  }

  listWishlist(collectionId: string): Observable<WishlistEntry[]> {
    return this.http.get<WishlistEntry[]>(`/api/v1/collections/${collectionId}/wishlist`);
  }

  createWishlistEntry(collectionId: string, request: {
    catalogItemId: string;
    priority: WishlistPriority;
    notes?: string | null;
  }): Observable<WishlistEntry> {
    return this.http.post<WishlistEntry>(`/api/v1/collections/${collectionId}/wishlist`, request);
  }

  updateWishlistEntry(collectionId: string, entryId: string, request: {
    catalogItemId: string;
    priority: WishlistPriority;
    notes?: string | null;
  }): Observable<WishlistEntry> {
    return this.http.put<WishlistEntry>(`/api/v1/collections/${collectionId}/wishlist/${entryId}`, request);
  }

  convertWishlistEntry(collectionId: string, entryId: string, request: {
    condition: CopyCondition;
    locationId?: string | null;
    acquiredOn: string;
    purchasePrice?: number | null;
    currentValue?: number | null;
    notes?: string | null;
  }): Observable<CollectionCopy> {
    return this.http.post<CollectionCopy>(
      `/api/v1/collections/${collectionId}/wishlist/${entryId}/convert`,
      request,
    );
  }

  deleteWishlistEntry(collectionId: string, entryId: string): Observable<void> {
    return this.http.delete<void>(`/api/v1/collections/${collectionId}/wishlist/${entryId}`);
  }
}