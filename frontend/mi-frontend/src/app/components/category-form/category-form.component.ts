// src/app/components/category-form/category-form.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { CategoryService, CategoryRequest, Category } from '../../services/category.service';
import { ContactService, Contact } from '../../services/contact.service';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './category-form.component.html',
  styleUrls: ['./category-form.component.css']
})
export class CategoryFormComponent implements OnInit {
  // Datos de la categoría
  category: CategoryRequest = {
    name: '',
    description: ''
  };

  // Gestión de contactos
  allContacts: Contact[] = [];
  selectedContactIds: Set<number> = new Set(); // ✅ NUEVO: Para manejar selección
  searchQuery = '';
  filteredContacts: Contact[] = [];

  // Estados
  isEditMode = false;
  categoryId: number | null = null;
  loading = false;
  saving = false;
  error = '';
  successMessage = '';

  // UI
  showContactSection = true; // ✅ Siempre mostrar la sección de contactos

  constructor(
    private categoryService: CategoryService,
    private contactService: ContactService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Cargar todos los contactos primero
    this.loadContacts();

    // Verificar si estamos en modo edición
    this.categoryId = Number(this.route.snapshot.params['id']);
    if (this.categoryId && !isNaN(this.categoryId)) {
      this.isEditMode = true;
      this.loadCategory();
    }
  }

  loadContacts(): void {
    this.contactService.getAllContacts().subscribe({
      next: (contacts) => {
        this.allContacts = contacts;
        this.filteredContacts = contacts;
      },
      error: (error) => {
        console.error('Error cargando contactos:', error);
        this.error = 'Error al cargar contactos';
      }
    });
  }

  loadCategory(): void {
    if (!this.categoryId) return;

    this.loading = true;
    this.error = '';

    // Cargar datos de la categoría
    this.categoryService.getCategoryById(this.categoryId).subscribe({
      next: (category) => {
        this.category = {
          name: category.name,
          description: category.description || ''
        };

        // Cargar contactos de esta categoría
        this.loadCategoryContacts();
      },
      error: (error) => {
        console.error('Error cargando categoría:', error);
        this.error = 'No se pudo cargar la categoría';
        this.loading = false;
      }
    });
  }

  loadCategoryContacts(): void {
    if (!this.categoryId) return;

    this.contactService.getContactsByCategory(this.categoryId).subscribe({
      next: (contacts) => {
        // Marcar contactos que ya están en la categoría
        this.selectedContactIds.clear();
        contacts.forEach(contact => {
          if (contact.id) {
            this.selectedContactIds.add(contact.id);
          }
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando contactos de categoría:', error);
        this.loading = false;
      }
    });
  }

  // ✅ NUEVO: Manejar selección/deselección de contactos
  toggleContactSelection(contactId: number): void {
    if (this.selectedContactIds.has(contactId)) {
      this.selectedContactIds.delete(contactId);

      // Si estamos en modo edición, remover inmediatamente
      if (this.isEditMode && this.categoryId) {
        this.removeContactFromCategory(contactId);
      }
    } else {
      this.selectedContactIds.add(contactId);

      // Si estamos en modo edición, agregar inmediatamente
      if (this.isEditMode && this.categoryId) {
        this.addContactToCategory(contactId);
      }
    }
  }

  isContactSelected(contactId: number): boolean {
    return this.selectedContactIds.has(contactId);
  }

  addContactToCategory(contactId: number): void {
    if (!this.categoryId) return;

    this.contactService.addContactToCategory(contactId, this.categoryId).subscribe({
      next: () => {
        // Ya está manejado en toggleContactSelection
      },
      error: (error) => {
        console.error('Error añadiendo contacto:', error);
        this.error = 'Error al añadir el contacto a la categoría';
        // Revertir selección
        this.selectedContactIds.delete(contactId);
      }
    });
  }

  removeContactFromCategory(contactId: number): void {
    if (!this.categoryId) return;

    this.contactService.removeContactFromCategory(contactId, this.categoryId).subscribe({
      next: () => {
        // Ya está manejado en toggleContactSelection
      },
      error: (error) => {
        console.error('Error removiendo contacto:', error);
        this.error = 'Error al remover el contacto de la categoría';
        // Revertir selección
        this.selectedContactIds.add(contactId);
      }
    });
  }

  searchContacts(): void {
    if (!this.searchQuery.trim()) {
      this.filteredContacts = this.allContacts;
      return;
    }

    this.filteredContacts = this.allContacts.filter(contact =>
      contact.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
      (contact.email && contact.email.toLowerCase().includes(this.searchQuery.toLowerCase())) ||
      (contact.phone && contact.phone.includes(this.searchQuery))
    );
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.filteredContacts = this.allContacts;
  }

  // ✅ MODIFICADO: Al guardar, también asignar contactos seleccionados
  onSubmit(): void {
    if (!this.isFormValid()) {
      this.error = 'Por favor completa el nombre de la categoría';
      return;
    }

    this.saving = true;
    this.error = '';
    this.successMessage = '';

    const operation = this.isEditMode
      ? this.categoryService.updateCategory(this.categoryId!, this.category)
      : this.categoryService.createCategory(this.category);

    operation.subscribe({
      next: (response) => {
        const savedCategoryId = this.isEditMode ? this.categoryId! : response.category?.id;

        if (savedCategoryId && !this.isEditMode) {
          // ✅ NUEVO: Si es una nueva categoría, asignar contactos seleccionados
          this.assignContactsToNewCategory(savedCategoryId);
        } else {
          this.saving = false;
          this.successMessage = this.isEditMode
            ? 'Categoría actualizada exitosamente'
            : 'Categoría creada exitosamente';

          setTimeout(() => {
            this.router.navigate(['/categories']);
          }, 2000);
        }
      },
      error: (error) => {
        console.error('Error guardando categoría:', error);
        this.saving = false;
        this.error = error.error?.message || 'Error al guardar la categoría';
      }
    });
  }

  // ✅ NUEVO: Asignar contactos a categoría recién creada
  assignContactsToNewCategory(categoryId: number): void {
    if (this.selectedContactIds.size === 0) {
      this.saving = false;
      this.successMessage = 'Categoría creada exitosamente';
      setTimeout(() => this.router.navigate(['/categories']), 2000);
      return;
    }

    // Asignar contactos uno por uno
    const contactIds = Array.from(this.selectedContactIds);
    let completed = 0;
    let errors = 0;

    contactIds.forEach(contactId => {
      this.contactService.addContactToCategory(contactId, categoryId).subscribe({
        next: () => {
          completed++;
          if (completed + errors === contactIds.length) {
            this.saving = false;
            this.successMessage = `Categoría creada exitosamente con ${completed} contactos asignados`;
            if (errors > 0) {
              this.successMessage += ` (${errors} contactos no se pudieron asignar)`;
            }
            setTimeout(() => this.router.navigate(['/categories']), 2000);
          }
        },
        error: () => {
          errors++;
          if (completed + errors === contactIds.length) {
            this.saving = false;
            this.successMessage = `Categoría creada exitosamente con ${completed} contactos asignados`;
            if (errors > 0) {
              this.successMessage += ` (${errors} contactos no se pudieron asignar)`;
            }
            setTimeout(() => this.router.navigate(['/categories']), 2000);
          }
        }
      });
    });
  }

  selectAllVisible(): void {
    this.filteredContacts.forEach(contact => {
      if (contact.id) {
        this.selectedContactIds.add(contact.id);
      }
    });
  }

  deselectAllVisible(): void {
    this.filteredContacts.forEach(contact => {
      if (contact.id) {
        this.selectedContactIds.delete(contact.id);
      }
    });
  }

  isFormValid(): boolean {
    return this.category.name.trim() !== '';
  }

  cancel(): void {
    this.router.navigate(['/categories']);
  }

  goBack(): void {
    this.cancel();
  }

  clearForm(): void {
    this.category = {
      name: '',
      description: ''
    };
    this.selectedContactIds.clear();
    this.error = '';
    this.successMessage = '';
  }

  getContactChannels(contact: Contact): string[] {
    const channels = [];
    if (contact.email) channels.push('📧');
    if (contact.phone) channels.push('📱');
    if (contact.whatsappId) channels.push('💬');
    return channels;
  }

  getSelectedContactsCount(): number {
    return this.selectedContactIds.size;
  }

  // ✅ NUEVOS: Métodos para estadísticas en la vista previa
  getSelectedContactsWithEmail(): number {
    return this.allContacts.filter(contact =>
      contact.id && this.selectedContactIds.has(contact.id) && contact.email
    ).length;
  }

  getSelectedContactsWithPhone(): number {
    return this.allContacts.filter(contact =>
      contact.id && this.selectedContactIds.has(contact.id) && contact.phone
    ).length;
  }

  trackByContactId(index: number, contact: Contact): number {
    return contact.id || index;
  }
}
