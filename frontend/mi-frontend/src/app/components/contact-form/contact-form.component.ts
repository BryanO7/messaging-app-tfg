// src/app/components/contact-form/contact-form.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { ContactService, ContactRequest } from '../../services/contact.service';

@Component({
  selector: 'app-contact-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './contact-form.component.html',
  styleUrls: ['./contact-form.component.css']
})
export class ContactFormComponent implements OnInit {
  contact: ContactRequest = {
    name: '',
    email: '',
    phone: '',
    whatsappId: '',
    notes: ''
  };

  isEditMode = false;
  contactId: number | null = null;
  loading = false;
  saving = false;
  error = '';
  successMessage = '';

  constructor(
    private contactService: ContactService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Verificar si estamos en modo edición
    this.contactId = Number(this.route.snapshot.params['id']);
    if (this.contactId && !isNaN(this.contactId)) {
      this.isEditMode = true;
      this.loadContact();
    }
  }

  loadContact(): void {
    if (!this.contactId) return;

    this.loading = true;
    this.error = '';

    this.contactService.getContactById(this.contactId).subscribe({
      next: (contact) => {
        this.contact = {
          name: contact.name,
          email: contact.email || '',
          phone: contact.phone || '',
          whatsappId: contact.whatsappId || '',
          notes: contact.notes || ''
        };
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando contacto:', error);
        this.error = 'No se pudo cargar el contacto';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (!this.isFormValid()) {
      this.error = 'Por favor completa todos los campos obligatorios';
      return;
    }

    this.saving = true;
    this.error = '';
    this.successMessage = '';

    const operation = this.isEditMode
      ? this.contactService.updateContact(this.contactId!, this.contact)
      : this.contactService.createContact(this.contact);

    operation.subscribe({
      next: (response) => {
        this.saving = false;
        this.successMessage = this.isEditMode
          ? 'Contacto actualizado exitosamente'
          : 'Contacto creado exitosamente';

        // Redirigir después de 2 segundos
        setTimeout(() => {
          this.router.navigate(['/contacts']);
        }, 2000);
      },
      error: (error) => {
        console.error('Error guardando contacto:', error);
        this.saving = false;

        if (error.error && error.error.message) {
          this.error = error.error.message;
        } else {
          this.error = 'Error al guardar el contacto. Intenta de nuevo.';
        }
      }
    });
  }

  isFormValid(): boolean {
    return this.contact.name.trim() !== '' &&
      (this.contact.email?.trim() !== '' || this.contact.phone?.trim() !== '');
  }

  cancel(): void {
    this.router.navigate(['/contacts']);
  }

  clearForm(): void {
    this.contact = {
      name: '',
      email: '',
      phone: '',
      whatsappId: '',
      notes: ''
    };
    this.error = '';
    this.successMessage = '';
  }
}
