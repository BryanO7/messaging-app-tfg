// src/app/components/category-list/category-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { CategoryService, Category } from '../../services/category.service';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {
  // Datos
  categories: Category[] = [];
  rootCategories: Category[] = [];
  filteredCategories: Category[] = [];

  // Configuración de vista
  viewMode: 'hierarchy' | 'all' = 'hierarchy';
  searchQuery = '';

  // Estados
  loading = false;
  error = '';

  constructor(
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.error = '';

    this.categoryService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.rootCategories = categories.filter(cat => !cat.parent);
        this.filteredCategories = [...this.categories];
        this.loading = false;
        console.log('📁 Categorías cargadas:', categories);
      },
      error: (error) => {
        console.error('Error cargando categorías:', error);
        this.error = 'Error al cargar las categorías';
        this.loading = false;
      }
    });
  }

  // === NAVEGACIÓN ===

  /**
   * ✅ NUEVO: Navegar a la vista de detalles de la categoría
   * Esta es la acción principal - gestionar contactos
   */
  viewCategoryDetail(categoryId: number): void {
    console.log('🔍 Navegando a detalles de categoría:', categoryId);
    this.router.navigate(['/categories', categoryId]);
  }

  /**
   * ✅ NUEVO: Navegar al formulario de edición
   * Para cambiar nombre/descripción de la categoría
   */
  editCategory(categoryId: number): void {
    console.log('✏️ Navegando a editar categoría:', categoryId);
    this.router.navigate(['/categories', categoryId, 'edit']);
  }

  /**
   * ✅ NUEVO: Crear subcategoría
   */
  createSubcategory(parentCategoryId: number): void {
    console.log('📂 Creando subcategoría para categoría:', parentCategoryId);
    this.router.navigate(['/categories/new'], {
      queryParams: { parent: parentCategoryId }  // ✅ CORREGIDO: usar 'parent'
    });
  }

  // === GESTIÓN DE CATEGORÍAS ===

  deleteCategory(categoryId: number): void {
    const category = this.categories.find(c => c.id === categoryId);
    const categoryName = category?.name || 'esta categoría';

    if (!confirm(`¿Estás seguro de que quieres eliminar "${categoryName}"?\n\nEsta acción no se puede deshacer.`)) {
      return;
    }

    console.log('🗑️ Eliminando categoría:', categoryId);

    this.categoryService.deleteCategory(categoryId).subscribe({
      next: () => {
        console.log('✅ Categoría eliminada exitosamente');

        // Actualizar las listas localmente
        this.categories = this.categories.filter(c => c.id !== categoryId);
        this.rootCategories = this.rootCategories.filter(c => c.id !== categoryId);
        this.applyFilters();

        // Mostrar mensaje de éxito (opcional)
        // this.showSuccessMessage(`Categoría "${categoryName}" eliminada`);
      },
      error: (error) => {
        console.error('❌ Error eliminando categoría:', error);
        this.error = `Error al eliminar la categoría "${categoryName}": ${error.message || 'Error desconocido'}`;
      }
    });
  }

  // === FILTROS Y BÚSQUEDA ===

  changeViewMode(mode: 'hierarchy' | 'all'): void {
    console.log('👁️ Cambiando modo de vista a:', mode);
    this.viewMode = mode;
    this.applyFilters();
  }

  searchCategories(): void {
    console.log('🔍 Buscando categorías con:', this.searchQuery);
    this.applyFilters();
  }

  clearSearch(): void {
    console.log('🔄 Limpiando búsqueda');
    this.searchQuery = '';
    this.applyFilters();
  }

  private applyFilters(): void {
    let baseCategories = this.viewMode === 'hierarchy' ? this.rootCategories : this.categories;

    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      this.filteredCategories = baseCategories.filter(category =>
        category.name.toLowerCase().includes(query) ||
        (category.description && category.description.toLowerCase().includes(query))
      );
    } else {
      this.filteredCategories = [...baseCategories];
    }

    console.log(`📊 Categorías filtradas: ${this.filteredCategories.length}/${baseCategories.length}`);
  }

  // === UTILIDADES PARA EL TEMPLATE ===

  getDisplayCategories(): Category[] {
    return this.viewMode === 'hierarchy' ? this.rootCategories : this.categories;
  }

  isRootCategory(category: Category): boolean {
    return !category.parent;
  }

  getFullCategoryName(category: Category): string {
    if (this.isRootCategory(category)) {
      return category.name;
    }

    // Usar la relación parent directamente
    if (category.parent) {
      return `${category.parent.name} → ${category.name}`;
    }

    return category.name;
  }

  trackByCategoryId(index: number, category: Category): number {
    return category.id!;
  }

  // === MÉTODOS DE AYUDA ===

  getCategoryStats(category: Category): string {
    // TODO: Si tienes información de contactos por categoría, la puedes mostrar aquí
    return `ID: ${category.id}`;
  }

  getCategoryTypeIcon(category: Category): string {
    return this.isRootCategory(category) ? '📁' : '📂';
  }

  getCategoryTypeName(category: Category): string {
    return this.isRootCategory(category) ? 'Categoría Principal' : 'Subcategoría';
  }

  // === DEBUGGING ===

  debugCategories(): void {
    console.log('🐛 DEBUG - Estado actual:');
    console.log('  📋 Todas las categorías:', this.categories);
    console.log('  🌳 Categorías raíz:', this.rootCategories);
    console.log('  🔍 Filtradas:', this.filteredCategories);
    console.log('  👁️ Modo de vista:', this.viewMode);
    console.log('  🔎 Búsqueda:', this.searchQuery);
  }
}
