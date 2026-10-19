import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { LucideDynamicIcon } from '@lucide/angular';
import { CatalogApi } from '../core/api.service';
import { uiIcons } from '../core/ui-icons';
import { AttributeDefinition, CatalogItem, IdentifierScheme, ItemType } from '../core/models';

@Component({
  selector: 'app-catalog-page',
  imports: [CommonModule, FormsModule, LucideDynamicIcon],
  styleUrl: './catalog-page.scss',
  templateUrl: './catalog-page.html',
})
export class CatalogPage implements OnInit {
  private readonly api = inject(CatalogApi);
  protected readonly icons = uiIcons;
  protected readonly types = signal<ItemType[]>([]);
  protected readonly items = signal<CatalogItem[]>([]);
  protected readonly filteredItems = computed(() => {
    const query = this.search().trim().toLocaleLowerCase();
    if (!query) return this.items();
    return this.items().filter((item) => `${item.title} ${item.itemTypeName}`.toLocaleLowerCase().includes(query));
  });
  protected readonly search = signal('');
  protected readonly selectedTypeId = signal('');
  protected readonly attributes = signal<AttributeDefinition[]>([]);
  protected readonly attributeValues = signal<Record<string, string>>({});
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');
  protected readonly editorOpen = signal(false);
  protected readonly editingItem = signal<CatalogItem | null>(null);
  protected readonly totalElements = signal(0);
  protected readonly totalPages = computed(() => Math.max(1, Math.ceil(this.totalElements() / this.pageSize)));
  protected readonly page = signal(0);
  protected readonly pageSize = 40;
  protected readonly identifierSchemes: IdentifierScheme[] = ['ISBN_10', 'ISBN_13', 'EAN_13', 'EDITION_CODE'];
  protected identifiers: Array<{ scheme: IdentifierScheme; value: string }> = [];
  protected title = '';
  protected description = '';

  ngOnInit(): void {
    this.api.listTypes().subscribe({
      next: (types) => {
        this.types.set(types.filter((type) => type.active));
        this.loadItems();
      },
      error: (error) => this.showError(error),
    });
  }

  protected loadItems(): void {
    this.loading.set(true);
    this.api.listItems(this.selectedTypeId() || null, this.page(), this.pageSize).subscribe({
      next: (result) => {
        this.items.set(result.content);
        this.totalElements.set(result.totalElements);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.showError(error);
      },
    });
  }

  protected filterByType(typeId: string): void {
    this.selectedTypeId.set(typeId);
    this.page.set(0);
    this.loadItems();
  }

  protected changePage(direction: number): void {
    this.page.update((page) => Math.max(0, page + direction));
    this.loadItems();
  }

  protected openCreate(): void {
    this.error.set('');
    this.success.set('');
    this.editingItem.set(null);
    this.title = '';
    this.description = '';
    this.identifiers = [];
    this.attributeValues.set({});
    const itemTypeId = this.selectedTypeId() || this.types()[0]?.id;
    this.selectedTypeId.set(itemTypeId ?? '');
    this.loadAttributes(itemTypeId);
    this.editorOpen.set(true);
  }

  protected openEdit(item: CatalogItem): void {
    this.error.set('');
    this.success.set('');
    this.editingItem.set(item);
    this.title = item.title;
    this.description = item.description ?? '';
    this.identifiers = item.identifiers.map(({ scheme, value }) => ({ scheme, value }));
    this.selectedTypeId.set(item.itemTypeId);
    this.loadAttributes(item.itemTypeId, item.attributes);
    this.editorOpen.set(true);
  }

  protected closeEditor(): void {
    this.editorOpen.set(false);
  }

  protected addIdentifier(): void {
    this.identifiers = [...this.identifiers, { scheme: 'ISBN_13', value: '' }];
  }

  protected removeIdentifier(index: number): void {
    this.identifiers = this.identifiers.filter((_identifier, current) => current !== index);
  }

  protected setAttribute(key: string, value: string): void {
    this.attributeValues.update((values) => ({ ...values, [key]: value }));
  }

  protected save(): void {
    const request = {
      title: this.title.trim(),
      description: this.description.trim() || null,
      identifiers: this.identifiers.filter((identifier) => identifier.value.trim())
        .map((identifier) => ({ ...identifier, value: identifier.value.trim() })),
      attributes: Object.fromEntries(Object.entries(this.attributeValues()).filter(([, value]) => value !== '')),
    };
    const editing = this.editingItem();
    this.saving.set(true);
    this.error.set('');
    const operation = editing
      ? this.api.updateItem(editing.id, request)
      : this.api.createItem({ ...request, itemTypeId: this.selectedTypeId() });
    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.editorOpen.set(false);
        this.success.set(editing ? 'Ficha actualizada.' : 'Ficha añadida al catálogo.');
        this.loadItems();
      },
      error: (error) => {
        this.saving.set(false);
        this.showError(error);
      },
    });
  }

  protected loadAttributes(itemTypeId?: string, existing: CatalogItem['attributes'] = []): void {
    this.attributes.set([]);
    if (!itemTypeId) return;
    this.api.listAttributes(itemTypeId).subscribe({
      next: (definitions) => {
        this.attributes.set(definitions);
        this.attributeValues.set(Object.fromEntries(existing.map((attribute) => [attribute.attributeKey, attribute.value])));
      },
      error: (error) => this.showError(error),
    });
  }

  private showError(error: unknown): void {
    const status = error instanceof HttpErrorResponse ? error.status : 0;
    this.error.set(status === 409
      ? 'El identificador ya está asociado a otra ficha.'
      : status === 404
        ? 'La ficha, el tipo o uno de sus atributos ya no existe.'
        : 'No se pudo completar la operación. Revisa los datos y la conexión con el catálogo.');
  }

  protected identifierLabel(identifier: CatalogItem['identifiers'][number]): string {
    return `${identifier.scheme}: ${identifier.value}`;
  }

  protected attributeLabel(attribute: CatalogItem['attributes'][number]): string {
    return `${attribute.name}: ${attribute.value}`;
  }
}