export interface Collector {
  collectorId: string;
  email: string;
  displayName: string;
  role: 'USER' | 'ADMIN';
}

export interface TokenResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresInSeconds: number;
}

export type AttributeDataType = 'TEXT' | 'INTEGER' | 'DECIMAL' | 'DATE' | 'BOOLEAN';
export type IdentifierScheme = 'ISBN_10' | 'ISBN_13' | 'EAN_13' | 'EDITION_CODE';

export interface ItemType {
  id: string;
  scope: 'GLOBAL' | 'PERSONAL';
  ownerId?: string | null;
  name: string;
  description?: string | null;
  active: boolean;
}

export interface AttributeDefinition {
  id: string;
  itemTypeId: string;
  name: string;
  attributeKey: string;
  dataType: AttributeDataType;
  required: boolean;
  displayOrder: number;
}

export interface CatalogIdentifier {
  scheme: IdentifierScheme;
  value: string;
}

export interface CatalogAttribute {
  name: string;
  attributeKey: string;
  dataType: AttributeDataType;
  value: string;
}

export interface CatalogItem {
  id: string;
  itemTypeId: string;
  itemTypeName: string;
  title: string;
  description?: string | null;
  imageReference?: string | null;
  createdAt: string;
  identifiers: CatalogIdentifier[];
  attributes: CatalogAttribute[];
}

export interface CatalogItemPage {
  content: CatalogItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export type CollectionVisibility = 'PRIVATE' | 'PUBLIC';

export interface CollectionResource {
  id: string;
  name: string;
  description?: string | null;
  visibility: CollectionVisibility;
  createdAt: string;
  updatedAt: string;
}

export interface Location {
  id: string;
  name: string;
  description?: string | null;
}

export type CopyCondition = 'NEW' | 'LIKE_NEW' | 'GOOD' | 'ACCEPTABLE' | 'POOR';

export interface CollectionCopy {
  id: string;
  catalogItemId: string;
  catalogItemTitle: string;
  condition: CopyCondition;
  locationId?: string | null;
  acquiredOn: string;
  purchasePrice?: number | null;
  currentValue?: number | null;
  notes?: string | null;
}

export type WishlistPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface WishlistEntry {
  id: string;
  catalogItemId: string;
  catalogItemTitle: string;
  priority: WishlistPriority;
  notes?: string | null;
}