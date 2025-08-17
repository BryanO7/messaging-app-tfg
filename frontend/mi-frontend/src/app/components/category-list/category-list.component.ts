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
  categories: Category[] = [];
  rootCategories: Category[] = [];
  loading = false;
  searchQuery = '';
  error = '';
  viewMode: 'all' | 'hierarchy' = 'hierarchy';

  constructor(
    public categoryService: CategoryService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.error = '';

    if (this.viewMode === 'hierarchy') {
      this.loadRootCategories();
    } else {
      this.loadAllCategories();
    }
  }

  loadAllCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando categorías:', error);
        this.error = 'Error al cargar las categorías';
        this.loading = false;
      }
    });
  }

  loadRootCategories(): void {
    this.categoryService.getRootCategories().subscribe({
      next: (categories) => {
        this.rootCategories = categories;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando categorías raíz:', error);
        this.error = 'Error al cargar las categorías';
        this.loading = false;
      }
    });
  }

  searchCategories(): void {
    if (this.searchQuery.trim() === '') {
      this.loadCategories();
      return;
    }

    this.loading = true;
    this.categoryService.searchCategories(this.searchQuery).subscribe({
      next: (categories) => {
        this.categories = categories;
        this.rootCategories = categories.filter(cat => !cat.parent);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error buscando categorías:', error);
        this.error = 'Error en la búsqueda';
        this.loading = false;
      }
    });
  }

  // ✅ NUEVO: Método para editar categoría al hacer clic
  editCategory(categoryId: number): void {
    this.router.navigate(['/categories', categoryId, 'edit']);
  }

  deleteCategory(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar esta categoría? Se eliminarán también sus subcategorías.')) {
      this.categoryService.deleteCategory(id).subscribe({
        next: () => {
          this.loadCategories(); // Recargar la lista
        },
        error: (error) => {
          console.error('Error eliminando categoría:', error);
          this.error = 'Error al eliminar la categoría';
        }
      });
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadCategories();
  }

  changeViewMode(mode: 'all' | 'hierarchy'): void {
    this.viewMode = mode;
    this.loadCategories();
  }

  trackByCategoryId(index: number, category: Category): number {
    return category.id || index;
  }

  getFullCategoryName(category: Category): string {
    return this.categoryService.getFullCategoryName(category);
  }

  isRootCategory(category: Category): boolean {
    return this.categoryService.isRootCategory(category);
  }

  // ✅ NUEVO: Obtener categorías según el modo de vista
  getDisplayCategories(): Category[] {
    return this.viewMode === 'hierarchy' ? this.rootCategories : this.categories;
  }
}
