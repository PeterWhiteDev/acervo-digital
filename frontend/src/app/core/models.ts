export interface Collector {
  collectorId: string;
  email: string;
  displayName: string;
  role: CollectorRole;
}

export type CollectorRole = 'USER' | 'ADMIN';
export const COLLECTOR_ROLE_LABELS: Record<CollectorRole, string> = {
  USER: 'Usuario',
  ADMIN: 'Administrador',
};

export interface AdminCollector extends Collector {
  active: boolean;
  createdAt: string;
}

export interface AdminCollectorCollectionsSummary {
  collectorId: string;
  collectionCount: number;
  collections: AdminCollectionSummary[];
}

export interface AdminCollectionSummary {
  collectionId: string;
  name: string;
  visibility: CollectionVisibility;
  itemCount: number;
  itemsByLocation: LocationItemCount[];
  unassignedItemCount: number;
}

export interface LocationItemCount {
  locationId: string;
  locationName: string;
  itemCount: number;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresInSeconds: number;
}

export type AttributeDataType = 'TEXT' | 'INTEGER' | 'DECIMAL' | 'DATE' | 'BOOLEAN';
export const ATTRIBUTE_DATA_TYPE_LABELS: Record<AttributeDataType, string> = {
  TEXT: 'Texto',
  INTEGER: 'Entero',
  DECIMAL: 'Decimal',
  DATE: 'Fecha',
  BOOLEAN: 'Sí / No',
};
export type IdentifierScheme = 'ISBN_10' | 'ISBN_13' | 'EAN_13' | 'EDITION_CODE';
export const IDENTIFIER_SCHEME_LABELS: Record<IdentifierScheme, string> = {
  ISBN_10: 'ISBN-10',
  ISBN_13: 'ISBN-13',
  EAN_13: 'EAN-13',
  EDITION_CODE: 'Código de edición',
};

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
export const COPY_CONDITION_LABELS: Record<CopyCondition, string> = {
  NEW: 'Nuevo',
  LIKE_NEW: 'Como nuevo',
  GOOD: 'Bueno',
  ACCEPTABLE: 'Aceptable',
  POOR: 'Malo',
};

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
export const WISHLIST_PRIORITY_LABELS: Record<WishlistPriority, string> = {
  LOW: 'Baja',
  MEDIUM: 'Media',
  HIGH: 'Alta',
};

export interface WishlistEntry {
  id: string;
  catalogItemId: string;
  catalogItemTitle: string;
  priority: WishlistPriority;
  notes?: string | null;
}