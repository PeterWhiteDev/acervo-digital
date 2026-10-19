import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { CatalogApi } from '../core/api.service';
import { AttributeDataType, AttributeDefinition, ItemType } from '../core/models';

@Component({
  selector: 'app-types-page',
  imports: [FormsModule],
  styleUrl: './types-page.scss',
  templateUrl: './types-page.html',
})
export class TypesPage implements OnInit {
  private readonly api = inject(CatalogApi);
  protected readonly types = signal<ItemType[]>([]);
  protected readonly attributes = signal<AttributeDefinition[]>([]);
  protected readonly selectedTypeId = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');
  protected readonly dataTypes: AttributeDataType[] = ['TEXT', 'INTEGER', 'DECIMAL', 'DATE', 'BOOLEAN'];
  protected typeName = '';
  protected typeDescription = '';
  protected attributeName = '';
  protected attributeKey = '';
  protected dataType: AttributeDataType = 'TEXT';
  protected required = false;

  protected get selectedType(): ItemType | undefined {
    return this.types().find((type) => type.id === this.selectedTypeId());
  }

  ngOnInit(): void {
    this.reloadTypes();
  }

  protected reloadTypes(): void {
    this.api.listTypes().subscribe({
      next: (types) => {
        this.types.set(types.filter((type) => type.active));
        if (!this.selectedTypeId() && types.length) {
          this.selectType(types[0].id);
        }
      },
      error: (error) => this.showError(error),
    });
  }

  protected selectType(id: string): void {
    this.selectedTypeId.set(id);
    this.loading.set(true);
    this.api.listAttributes(id).subscribe({
      next: (attributes) => {
        this.attributes.set(attributes);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.showError(error);
      },
    });
  }

  protected createType(): void {
    this.error.set('');
    this.success.set('');
    this.api.createType({ name: this.typeName.trim(), description: this.typeDescription.trim() || null }).subscribe({
      next: (type) => {
        this.typeName = '';
        this.typeDescription = '';
        this.success.set('Tipo personal creado.');
        this.reloadTypes();
        this.selectType(type.id);
      },
      error: (error) => this.showError(error),
    });
  }

  protected createAttribute(): void {
    const typeId = this.selectedTypeId();
    if (!typeId) return;
    this.error.set('');
    this.success.set('');
    this.api.createAttribute(typeId, {
      name: this.attributeName.trim(),
      attributeKey: this.attributeKey.trim(),
      dataType: this.dataType,
      required: this.required,
      displayOrder: this.attributes().length,
    }).subscribe({
      next: () => {
        this.attributeName = '';
        this.attributeKey = '';
        this.required = false;
        this.success.set('Definición añadida.');
        this.selectType(typeId);
      },
      error: (error) => this.showError(error),
    });
  }

  protected makeKey(): void {
    this.attributeKey = this.attributeName
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLocaleLowerCase()
      .replace(/[^a-z0-9]+/g, '_')
      .replace(/^_|_$/g, '');
  }

  private showError(error: unknown): void {
    const status = error instanceof HttpErrorResponse ? error.status : 0;
    this.error.set(status === 403
      ? 'No se pueden modificar las plantillas globales.'
      : status === 409
        ? 'Ya existe un tipo o una clave de atributo con ese nombre.'
        : 'No se pudo completar la operación. Comprueba los datos y el servicio de catálogo.');
  }
}