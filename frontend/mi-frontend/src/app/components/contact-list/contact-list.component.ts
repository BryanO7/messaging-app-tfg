// src/app/components/contact-list/contact-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ContactService, Contact } from '../../services/contact.service';

@Component({
  selector: 'app-contact-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './contact-list.component.html',
  styleUrls: ['./contact-list.component.css']
})
export class ContactListComponent implements OnInit {
  contacts: Contact[] = [];
  loading = false;
  searchQuery = '';
  error = '';

  constructor(private contactService: ContactService) { }

  ngOnInit(): void {
    this.loadContacts();
  }

  loadContacts(): void {
    this.loading = true;
    this.error = '';

    this.contactService.getAllContacts().subscribe({
      next: (contacts) => {
        this.contacts = contacts;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando contactos:', error);
        this.error = 'Error al cargar los contactos';
        this.loading = false;
      }
    });
  }

  searchContacts(): void {
    if (this.searchQuery.trim() === '') {
      this.loadContacts();
      return;
    }

    this.loading = true;
    this.contactService.searchContacts(this.searchQuery).subscribe({
      next: (contacts) => {
        this.contacts = contacts;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error buscando contactos:', error);
        this.error = 'Error en la búsqueda';
        this.loading = false;
      }
    });
  }

  deleteContact(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este contacto?')) {
      this.contactService.deleteContact(id).subscribe({
        next: () => {
          this.loadContacts(); // Recargar la lista
        },
        error: (error) => {
          console.error('Error eliminando contacto:', error);
          this.error = 'Error al eliminar el contacto';
        }
      });
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadContacts();
  }

  trackByContactId(index: number, contact: Contact): number {
    return contact.id || index;
  }
}
