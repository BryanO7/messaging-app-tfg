// src/app/services/category.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Category {
  id?: number;
  name: string;
  description?: string;
  parent?: Category;
  createdAt?: string;
}

export interface CategoryRequest {
  name: string;
  description?: string;
  parentId?: number;
}

export interface CategoryStats {
  totalContacts: number;
  contactsWithEmail: number;
  contactsWithPhone: number;
  subcategoriesCount: number;
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = 'http://localhost:8080/api/categories';

  constructor(private http: HttpClient) { }

  // === CRUD BÁSICO ===

  // Obtener todas las categorías
  getAllCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.apiUrl);
  }

  // Obtener categorías raíz (sin padre)
  getRootCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/root`);
  }

  // Obtener categoría por ID
  getCategoryById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/${id}`);
  }

  // Obtener categoría con sus contactos
  getCategoryWithContacts(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/${id}/with-contacts`);
  }

  // Crear nueva categoría
  createCategory(category: CategoryRequest): Observable<any> {
    return this.http.post<any>(this.apiUrl, category);
  }

  // Actualizar categoría
  updateCategory(id: number, category: CategoryRequest): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, category);
  }

  // Eliminar categoría
  deleteCategory(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  // === FUNCIONALIDADES ESPECÍFICAS ===

  // Obtener subcategorías de una categoría
  getSubcategories(parentId: number): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/${parentId}/subcategories`);
  }

  // Buscar categorías
  searchCategories(query: string): Observable<Category[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<Category[]>(`${this.apiUrl}/search`, { params });
  }

  // Obtener estadísticas de una categoría
  getCategoryStats(id: number): Observable<CategoryStats> {
    return this.http.get<CategoryStats>(`${this.apiUrl}/${id}/stats`);
  }

  // Obtener canales de comunicación disponibles en una categoría
  getCategoryChannels(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}/channels`);
  }

  // === MÉTODOS DE UTILIDAD ===

  // Verificar si una categoría es raíz
  isRootCategory(category: Category): boolean {
    return !category.parent;
  }

  // Obtener el nombre completo de la categoría (incluyendo jerarquía)
  getFullCategoryName(category: Category): string {
    if (!category.parent) {
      return category.name;
    }
    return `${this.getFullCategoryName(category.parent)} > ${category.name}`;
  }
}
