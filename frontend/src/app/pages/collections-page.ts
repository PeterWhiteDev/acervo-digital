import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { LucideDynamicIcon } from '@lucide/angular';
import { CollectionApi, CatalogApi } from '../core/api.service';
import { uiIcons } from '../core/ui-icons';
import {
  CatalogItem,
  CollectionCopy,
  CollectionResource,
  CopyCondition,
  Location,
  WishlistEntry,
  WishlistPriority,
} from '../core/models';

type CollectionTab = 'copies' | 'wishlist' | 'locations';
type EditorMode = 'collection' | 'copy' | 'wish' | 'convert' | null;

@Component({
  selector: 'app-collections-page',
  imports: [CommonModule, FormsModule, LucideDynamicIcon],
  styleUrl: './collections-page.scss',
  templateUrl: './collections-page.html',
})
export class CollectionsPage implements OnInit {
  private readonly api = inject(CollectionApi);
  private readonly catalogApi = inject(CatalogApi);
  protected readonly icons = uiIcons;
  protected readonly collections = signal<CollectionResource[]>([]);
  protected readonly copies = signal<CollectionCopy[]>([]);
  protected readonly wishlist = signal<WishlistEntry[]>([]);
  protected readonly locations = signal<Location[]>([]);
  protected readonly catalogItems = signal<CatalogItem[]>([]);
  protected readonly selectedCollectionId = signal('');
  protected readonly activeTab = signal<CollectionTab>('copies');
  protected readonly editorMode = signal<EditorMode>(null);
  protected readonly editingCopy = signal<CollectionCopy | null>(null);
  protected readonly editingWish = signal<WishlistEntry | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');
  protected readonly selectedCollection = computed(() =>
    this.collections().find((collection) => collection.id === this.selectedCollectionId()),
  );
  protected readonly conditions: CopyCondition[] = ['NEW', 'LIKE_NEW', 'GOOD', 'ACCEPTABLE', 'POOR'];
  protected readonly priorities: WishlistPriority[] = ['LOW', 'MEDIUM', 'HIGH'];
  protected readonly today = new Date().toISOString().slice(0, 10);
  protected collectionName = '';
  protected collectionDescription = '';
  protected visibility: 'PRIVATE' | 'PUBLIC' = 'PRIVATE';
  protected catalogItemId = '';
  protected condition: CopyCondition = 'GOOD';
  protected locationId = '';
  protected acquiredOn = this.today;
  protected purchasePrice: number | null = null;
  protected currentValue: number | null = null;
  protected notes = '';
  protected priority: WishlistPriority = 'MEDIUM';
  protected locationName = '';
  protected locationDescription = '';

  ngOnInit(): void {
    this.loadCollections();
    this.loadCatalogItems();
    this.loadLocations();
  }

  protected loadCollections(): void {
    this.api.listCollections().subscribe({
      next: (collections) => {
        this.collections.set(collections);
        if (!this.selectedCollectionId() && collections.length) {
          this.selectCollection(collections[0].id);
        }
      },
      error: (error) => this.showError(error),
    });
  }

  protected selectCollection(id: string): void {
    this.selectedCollectionId.set(id);
    this.loadCollectionData();
  }

  protected setTab(tab: CollectionTab): void {
    this.activeTab.set(tab);
    this.error.set('');
  }

  protected openCollectionEditor(): void {
    this.resetEditor();
    this.editorMode.set('collection');
  }

  protected openCopyEditor(copy: CollectionCopy | null = null): void {
    this.resetEditor();
    this.editingCopy.set(copy);
    if (copy) {
      this.catalogItemId = copy.catalogItemId;
      this.condition = copy.condition;
      this.locationId = copy.locationId ?? '';
      this.acquiredOn = copy.acquiredOn;
      this.purchasePrice = copy.purchasePrice ?? null;
      this.currentValue = copy.currentValue ?? null;
      this.notes = copy.notes ?? '';
    }
    this.editorMode.set('copy');
  }

  protected openWishEditor(entry: WishlistEntry | null = null): void {
    this.resetEditor();
    this.editingWish.set(entry);
    if (entry) {
      this.catalogItemId = entry.catalogItemId;
      this.priority = entry.priority;
      this.notes = entry.notes ?? '';
    }
    this.editorMode.set('wish');
  }

  protected openConvertEditor(entry: WishlistEntry): void {
    this.resetEditor();
    this.editingWish.set(entry);
    this.catalogItemId = entry.catalogItemId;
    this.notes = '';
    this.editorMode.set('convert');
  }

  protected closeEditor(): void {
    this.editorMode.set(null);
  }

  protected saveCollection(): void {
    this.saving.set(true);
    this.api.createCollection({
      name: this.collectionName.trim(),
      description: this.collectionDescription.trim() || null,
      visibility: this.visibility,
    }).subscribe({
      next: (collection) => {
        this.saving.set(false);
        this.editorMode.set(null);
        this.success.set('Colección creada.');
        this.collections.update((collections) => [collection, ...collections]);
        this.selectCollection(collection.id);
      },
      error: (error) => this.fail(error),
    });
  }

  protected saveCopy(): void {
    const collectionId = this.selectedCollectionId();
    const request = {
      catalogItemId: this.catalogItemId,
      condition: this.condition,
      locationId: this.locationId || null,
      acquiredOn: this.acquiredOn,
      purchasePrice: this.purchasePrice,
      currentValue: this.currentValue,
      notes: this.notes.trim() || null,
    };
    const copy = this.editingCopy();
    this.saving.set(true);
    const operation = copy
      ? this.api.updateCopy(collectionId, copy.id, request)
      : this.api.createCopy(collectionId, request);
    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.editorMode.set(null);
        this.success.set(copy ? 'Copia actualizada.' : 'Copia añadida.');
        this.loadCopies();
      },
      error: (error) => this.fail(error),
    });
  }

  protected saveWish(): void {
    const collectionId = this.selectedCollectionId();
    const request = {
      catalogItemId: this.catalogItemId,
      priority: this.priority,
      notes: this.notes.trim() || null,
    };
    const entry = this.editingWish();
    this.saving.set(true);
    const operation = entry
      ? this.api.updateWishlistEntry(collectionId, entry.id, request)
      : this.api.createWishlistEntry(collectionId, request);
    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.editorMode.set(null);
        this.success.set(entry ? 'Deseo actualizado.' : 'Deseo añadido.');
        this.loadWishlist();
      },
      error: (error) => this.fail(error),
    });
  }

  protected convertWish(): void {
    const entry = this.editingWish();
    if (!entry) return;
    this.saving.set(true);
    this.api.convertWishlistEntry(this.selectedCollectionId(), entry.id, {
      condition: this.condition,
      locationId: this.locationId || null,
      acquiredOn: this.acquiredOn,
      purchasePrice: this.purchasePrice,
      currentValue: this.currentValue,
      notes: this.notes.trim() || null,
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.editorMode.set(null);
        this.success.set('Deseo convertido en copia.');
        this.loadCollectionData();
      },
      error: (error) => this.fail(error),
    });
  }

  protected deleteCollection(collection: CollectionResource): void {
    if (!window.confirm(`¿Eliminar “${collection.name}” y todas sus copias y deseos?`)) return;
    this.api.deleteCollection(collection.id).subscribe({
      next: () => {
        this.collections.update((items) => items.filter((item) => item.id !== collection.id));
        this.selectedCollectionId.set('');
        this.success.set('Colección eliminada.');
        if (this.collections().length) this.selectCollection(this.collections()[0].id);
        else this.clearCollectionData();
      },
      error: (error) => this.showError(error),
    });
  }

  protected deleteCopy(copy: CollectionCopy): void {
    if (!window.confirm(`¿Eliminar la copia de “${copy.catalogItemTitle}”?`)) return;
    this.api.deleteCopy(this.selectedCollectionId(), copy.id).subscribe({
      next: () => {
        this.success.set('Copia eliminada.');
        this.loadCopies();
      },
      error: (error) => this.showError(error),
    });
  }

  protected deleteWish(entry: WishlistEntry): void {
    if (!window.confirm(`¿Eliminar “${entry.catalogItemTitle}” de la lista de deseos?`)) return;
    this.api.deleteWishlistEntry(this.selectedCollectionId(), entry.id).subscribe({
      next: () => {
        this.success.set('Deseo eliminado.');
        this.loadWishlist();
      },
      error: (error) => this.showError(error),
    });
  }

  protected saveLocation(): void {
    this.saving.set(true);
    this.api.createLocation({
      name: this.locationName.trim(),
      description: this.locationDescription.trim() || null,
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.locationName = '';
        this.locationDescription = '';
        this.success.set('Ubicación creada.');
        this.loadLocations();
      },
      error: (error) => this.fail(error),
    });
  }

  private loadCollectionData(): void {
    this.loadCopies();
    this.loadWishlist();
  }

  private loadCopies(): void {
    const id = this.selectedCollectionId();
    if (!id) return this.copies.set([]);
    this.loading.set(true);
    this.api.listCopies(id).subscribe({
      next: (copies) => { this.copies.set(copies); this.loading.set(false); },
      error: (error) => { this.loading.set(false); this.showError(error); },
    });
  }

  private loadWishlist(): void {
    const id = this.selectedCollectionId();
    if (!id) return this.wishlist.set([]);
    this.api.listWishlist(id).subscribe({
      next: (entries) => this.wishlist.set(entries),
      error: (error) => this.showError(error),
    });
  }

  private loadCatalogItems(): void {
    this.catalogApi.listItems(null, 0, 100).subscribe({
      next: (result) => this.catalogItems.set(result.content),
      error: () => this.catalogItems.set([]),
    });
  }

  private loadLocations(): void {
    this.api.listLocations().subscribe({
      next: (locations) => this.locations.set(locations),
      error: (error) => this.showError(error),
    });
  }

  private clearCollectionData(): void {
    this.copies.set([]);
    this.wishlist.set([]);
  }

  private resetEditor(): void {
    this.error.set('');
    this.editingCopy.set(null);
    this.editingWish.set(null);
    this.catalogItemId = this.catalogItems()[0]?.id ?? '';
    this.condition = 'GOOD';
    this.locationId = '';
    this.acquiredOn = this.today;
    this.purchasePrice = null;
    this.currentValue = null;
    this.notes = '';
    this.priority = 'MEDIUM';
    this.collectionName = '';
    this.collectionDescription = '';
    this.visibility = 'PRIVATE';
  }

  private fail(error: unknown): void {
    this.saving.set(false);
    this.showError(error);
  }

  private showError(error: unknown): void {
    const status = error instanceof HttpErrorResponse ? error.status : 0;
    this.error.set(status === 403
      ? 'No tienes permiso para modificar esta colección.'
      : status === 404
        ? 'La colección, ubicación o ficha ya no existe.'
        : status === 503
          ? 'El catálogo no está disponible en este momento.'
          : 'No se pudo completar la operación. Comprueba los datos e inténtalo de nuevo.');
  }
}