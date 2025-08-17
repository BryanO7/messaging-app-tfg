// src/app/components/message-form/message-form.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';
import { ContactService, Contact } from '../../services/contact.service';
import { CategoryService, Category } from '../../services/category.service';
import { MessageService, MessageRequest } from '../../services/message.service';

@Component({
  selector: 'app-message-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './message-form.component.html',
  styleUrls: ['./message-form.component.css']
})
export class MessageFormComponent implements OnInit {
  // Datos del formulario
  message: MessageRequest = {
    recipientType: '',
    recipientValue: '',
    channel: '',
    subject: '',
    content: '',
    scheduledTime: ''
  };

  // Modo de envío
  sendingMode: 'now' | 'scheduled' = 'now';

  // Datos de la aplicación
  contacts: Contact[] = [];
  categories: Category[] = [];

  // Para selección múltiple
  selectedContacts: Contact[] = [];
  showContactsDropdown = false;
  contactSearchQuery = '';
  filteredContacts: Contact[] = [];

  // Estados del formulario
  loading = false;
  sending = false;
  success = false;
  error = '';
  successMessage = '';

  // UI
  showPreview = false;
  characterCount = 0;
  estimatedCost = 0;

  constructor(
    private contactService: ContactService,
    private categoryService: CategoryService,
    private messageService: MessageService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.checkQueryParams();
  }

  loadData(): void {
    this.loading = true;

    forkJoin({
      contacts: this.contactService.getAllContacts(),
      categories: this.categoryService.getAllCategories()
    }).subscribe({
      next: (result) => {
        this.contacts = result.contacts || [];
        this.categories = result.categories || [];
        this.filteredContacts = [...this.contacts];
        this.loading = false;
        console.log('📊 Datos cargados:', { contacts: this.contacts.length, categories: this.categories.length });
      },
      error: (error) => {
        console.error('Error cargando datos:', error);
        this.error = 'Error al cargar contactos y categorías';
        this.loading = false;
      }
    });
  }

  checkQueryParams(): void {
    // Pre-seleccionar basado en query params de la URL
    const recipientType = this.route.snapshot.queryParams['recipientType'];
    const recipientValue = this.route.snapshot.queryParams['recipientValue'];

    if (recipientType && recipientValue) {
      this.message.recipientType = recipientType;
      this.message.recipientValue = recipientValue;
    }
  }

  // === EVENTOS DE CAMBIO ===

  onRecipientTypeChange(): void {
    console.log('🔄 Tipo de destinatario cambiado:', this.message.recipientType);

    // Limpiar selecciones anteriores
    this.message.recipientValue = '';
    this.selectedContacts = [];
    this.showContactsDropdown = false;

    // Actualizar disponibilidad de canales
    this.updateChannelAvailability();
  }

  onRecipientChange(): void {
    console.log('🔄 Destinatario cambiado:', this.message.recipientValue);
    this.updateChannelAvailability();
    this.calculateCost();
  }

  onChannelChange(): void {
    console.log('🔄 Canal cambiado:', this.message.channel);
    this.calculateCost();
  }

  onSendingModeChange(): void {
    console.log('🔄 Modo de envío cambiado:', this.sendingMode);
    if (this.sendingMode === 'now') {
      this.message.scheduledTime = '';
    }
  }

  // === SELECCIÓN MÚLTIPLE DE CONTACTOS ===

  toggleContactsDropdown(): void {
    this.showContactsDropdown = !this.showContactsDropdown;
    if (this.showContactsDropdown) {
      this.filterContacts();
    }
  }

  filterContacts(): void {
    if (!this.contactSearchQuery) {
      this.filteredContacts = [...this.contacts];
    } else {
      const query = this.contactSearchQuery.toLowerCase();
      this.filteredContacts = this.contacts.filter(contact =>
        contact.name.toLowerCase().includes(query) ||
        (contact.email && contact.email.toLowerCase().includes(query)) ||
        (contact.phone && contact.phone.includes(query))
      );
    }
  }

  isContactSelected(contactId: number): boolean {
    return this.selectedContacts.some(c => c.id === contactId);
  }

  toggleContactSelection(contactId: number): void {
    const contact = this.contacts.find(c => c.id === contactId);
    if (!contact) return;

    const index = this.selectedContacts.findIndex(c => c.id === contactId);
    if (index > -1) {
      this.selectedContacts.splice(index, 1);
    } else {
      this.selectedContacts.push(contact);
    }

    console.log('👥 Contactos seleccionados:', this.selectedContacts.length);
    this.updateChannelAvailability();
    this.calculateCost();
  }

  removeContactSelection(contactId: number): void {
    const index = this.selectedContacts.findIndex(c => c.id === contactId);
    if (index > -1) {
      this.selectedContacts.splice(index, 1);
      this.updateChannelAvailability();
      this.calculateCost();
    }
  }

  // === VALIDACIONES Y DISPONIBILIDAD ===

  updateChannelAvailability(): void {
    // Lógica para determinar qué canales están disponibles
    // basado en los destinatarios seleccionados
  }

  canUseEmail(): boolean {
    if (this.message.recipientType === 'individual') {
      const contact = this.contacts.find(c => c.id === parseInt(this.message.recipientValue));
      return !!contact?.email;
    }
    if (this.message.recipientType === 'multiple') {
      return this.selectedContacts.some(c => c.email);
    }
    return true; // Para categorías, asumimos que hay emails
  }

  canUseSms(): boolean {
    if (this.message.recipientType === 'individual') {
      const contact = this.contacts.find(c => c.id === parseInt(this.message.recipientValue));
      return !!contact?.phone;
    }
    if (this.message.recipientType === 'multiple') {
      return this.selectedContacts.some(c => c.phone);
    }
    return true; // Para categorías, asumimos que hay teléfonos
  }

  canUseBoth(): boolean {
    return this.canUseEmail() && this.canUseSms();
  }

  showSubjectField(): boolean {
    return this.message.channel === 'email' || this.message.channel === 'both';
  }

  isFormValid(): boolean {
    // Verificar tipo de destinatario
    if (!this.message.recipientType) return false;

    // Verificar destinatario específico
    if (this.message.recipientType === 'individual' && !this.message.recipientValue) return false;
    if (this.message.recipientType === 'category' && !this.message.recipientValue) return false;
    if (this.message.recipientType === 'multiple' && this.selectedContacts.length === 0) return false;

    // Verificar canal
    if (!this.message.channel) return false;

    // Verificar contenido
    if (!this.message.content.trim()) return false;

    // Verificar asunto si es email
    if (this.showSubjectField() && !this.message.subject?.trim()) return false;

    // Verificar fecha programada si es necesaria
    if (this.sendingMode === 'scheduled' && !this.message.scheduledTime) return false;

    return true;
  }

  // === CÁLCULOS Y ESTIMACIONES ===

  updateCharacterCount(): void {
    this.characterCount = this.message.content.length;
    this.calculateCost();
  }

  getEstimatedRecipients(): number {
    if (this.message.recipientType === 'individual') {
      return this.message.recipientValue ? 1 : 0;
    }
    if (this.message.recipientType === 'multiple') {
      return this.selectedContacts.length;
    }
    if (this.message.recipientType === 'category') {
      // TODO: Obtener número real de contactos en la categoría
      return 1; // Placeholder
    }
    return 0;
  }

  getEmailCount(): number {
    if (this.message.recipientType === 'individual') {
      const contact = this.contacts.find(c => c.id === parseInt(this.message.recipientValue));
      return contact?.email ? 1 : 0;
    }
    if (this.message.recipientType === 'multiple') {
      return this.selectedContacts.filter(c => c.email).length;
    }
    return this.getEstimatedRecipients(); // Para categorías
  }

  getSmsCount(): number {
    if (this.message.recipientType === 'individual') {
      const contact = this.contacts.find(c => c.id === parseInt(this.message.recipientValue));
      return contact?.phone ? 1 : 0;
    }
    if (this.message.recipientType === 'multiple') {
      return this.selectedContacts.filter(c => c.phone).length;
    }
    return this.getEstimatedRecipients(); // Para categorías
  }

  calculateCost(): void {
    const recipients = this.getEstimatedRecipients();
    this.estimatedCost = this.messageService.estimateMessageCost(this.message, recipients);
  }

  getMaxCharacters(): number {
    return this.message.channel === 'sms' ? 160 : 2000;
  }

  // === ENVÍO DEL MENSAJE ===

  onSend(): void {
    if (!this.isFormValid()) {
      this.error = 'Por favor completa todos los campos requeridos';
      return;
    }

    this.sending = true;
    this.error = '';

    // Preparar datos según el tipo de destinatario
    const messageData = this.prepareMessageData();

    console.log('🚀 Enviando mensaje:', messageData);

    this.messageService.sendMessage(messageData).subscribe({
      next: (response) => {
        console.log('✅ Mensaje enviado exitosamente:', response);
        this.sending = false;
        this.success = true;
        this.successMessage = this.sendingMode === 'scheduled'
          ? 'Mensaje programado exitosamente'
          : 'Mensaje enviado exitosamente';

        // Limpiar formulario después de 3 segundos
        setTimeout(() => {
          this.resetForm();
        }, 3000);
      },
      error: (error) => {
        console.error('❌ Error enviando mensaje:', error);
        this.sending = false;
        this.error = error.error?.message || 'Error al enviar el mensaje';
      }
    });
  }

  private prepareMessageData(): MessageRequest {
    const data: MessageRequest = {
      recipientType: this.message.recipientType as any,
      recipientValue: this.message.recipientValue,
      channel: this.message.channel as any,
      subject: this.message.subject,
      content: this.message.content,
      scheduledTime: this.sendingMode === 'scheduled' ? this.message.scheduledTime : undefined
    };

    // Para múltiples contactos, enviar IDs
    if (this.message.recipientType === 'multiple') {
      data.recipientValue = this.selectedContacts.map(c => c.id).join(',');
    }

    return data;
  }

  resetForm(): void {
    this.message = {
      recipientType: '',
      recipientValue: '',
      channel: '',
      subject: '',
      content: '',
      scheduledTime: ''
    };
    this.sendingMode = 'now';
    this.selectedContacts = [];
    this.showContactsDropdown = false;
    this.contactSearchQuery = '';
    this.showPreview = false;
    this.characterCount = 0;
    this.estimatedCost = 0;
    this.success = false;
    this.error = '';
  }

  // === UTILIDADES ===

  getMinScheduleDateTime(): string {
    const now = new Date();
    now.setMinutes(now.getMinutes() + 5);
    return now.toISOString().slice(0, 16);
  }

  getChannelName(channel: string): string {
    const names = {
      email: '📧 Email',
      sms: '📱 SMS',
      both: '📧📱 Email + SMS'
    };
    return names[channel as keyof typeof names] || channel;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }
}
