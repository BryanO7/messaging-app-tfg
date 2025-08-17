// src/app/components/message-form/message-form.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { ContactService, Contact } from '../../services/contact.service';
import { CategoryService, Category } from '../../services/category.service';
import { MessageService, MessageRequest } from '../../services/message.service';

interface RecipientOption {
  type: 'individual' | 'category' | 'all';
  value: string;
  label: string;
  description: string;
  contactCount?: number;
  hasEmail?: boolean;
  hasPhone?: boolean;
}

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
    recipientType: 'individual',
    recipientValue: '',
    channel: 'email',
    subject: '',
    content: '',
    scheduledTime: ''
  };

  // Opciones disponibles
  contacts: Contact[] = [];
  categories: Category[] = [];
  recipientOptions: RecipientOption[] = [];
  selectedRecipient: RecipientOption | null = null;

  // Datos filtrados para el template
  filteredRecipientOptions: RecipientOption[] = [];

  // Estados
  loading = false;
  sending = false;
  success = false;
  error = '';

  // UI
  showPreview = false;
  showScheduling = false;
  characterCount = 0;
  estimatedCost = 0;
  estimatedRecipients = 0;

  // ✅ NUEVO: Referencia a Math para el template
  Math = Math;

  constructor(
    private contactService: ContactService,
    private categoryService: CategoryService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.updateCharacterCount();
  }

  loadData(): void {
    this.loading = true;

    // ✅ CORREGIDO: Usar forkJoin en lugar de Promise.all con toPromise()
    forkJoin({
      contacts: this.contactService.getAllContacts(),
      categories: this.categoryService.getAllCategories()
    }).subscribe({
      next: (result) => {
        this.contacts = result.contacts || [];
        this.categories = result.categories || [];

        // Pasar contactos al MessageService para caché
        this.messageService.setContactCache(this.contacts);

        this.setupRecipientOptions();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando datos:', error);
        this.error = 'Error al cargar contactos y categorías';
        this.loading = false;
      }
    });
  }

  setupRecipientOptions(): void {
    this.recipientOptions = [];

    // Opción: Todos los contactos
    if (this.contacts.length > 0) {
      const emailCount = this.contacts.filter(c => c.email).length;
      const phoneCount = this.contacts.filter(c => c.phone).length;

      this.recipientOptions.push({
        type: 'all',
        value: 'all',
        label: '🌍 Todos los contactos',
        description: `${this.contacts.length} contactos total`,
        contactCount: this.contacts.length,
        hasEmail: emailCount > 0,
        hasPhone: phoneCount > 0
      });
    }

    // Categorías
    this.categories.forEach(category => {
      this.recipientOptions.push({
        type: 'category',
        value: category.id!.toString(),
        label: `📁 ${category.name}`,
        description: category.description || 'Sin descripción',
        contactCount: 0, // Se actualizará cuando se seleccione
        hasEmail: true, // Asumimos que sí por ahora
        hasPhone: true
      });
    });

    // ✅ CORREGIDO: Contactos individuales con información detallada
    this.contacts.forEach(contact => {
      this.recipientOptions.push({
        type: 'individual',
        value: contact.id!.toString(),
        label: `👤 ${contact.name}`,
        description: this.getContactDescription(contact),
        contactCount: 1,
        hasEmail: !!contact.email,
        hasPhone: !!contact.phone
      });
    });

    // ✅ NUEVO: Debug logging
    console.log('📋 Opciones de destinatarios configuradas:', this.recipientOptions);
    console.log('👥 Contactos cargados:', this.contacts);
  }

  getContactDescription(contact: Contact): string {
    const channels = [];
    if (contact.email) channels.push(`📧 ${contact.email}`);
    if (contact.phone) channels.push(`📱 ${contact.phone}`);
    if (contact.whatsappId) channels.push(`💬 ${contact.whatsappId}`);
    return channels.length > 0 ? channels.join(' • ') : 'Sin canales de contacto';
  }

  onRecipientChange(): void {
    console.log('🔄 Cambio de destinatario:', {
      type: this.message.recipientType,
      value: this.message.recipientValue
    });

    this.selectedRecipient = this.recipientOptions.find(
      opt => opt.type === this.message.recipientType && opt.value === this.message.recipientValue
    ) || null;

    console.log('✅ Destinatario seleccionado:', this.selectedRecipient);

    // ✅ NUEVO: Si es contacto individual, obtener y mostrar información detallada
    if (this.message.recipientType === 'individual' && this.message.recipientValue) {
      const contact = this.contacts.find(c => c.id === parseInt(this.message.recipientValue));
      console.log('👤 Contacto encontrado:', contact);

      if (contact && this.selectedRecipient) {
        this.selectedRecipient.hasEmail = !!contact.email;
        this.selectedRecipient.hasPhone = !!contact.phone;
        this.selectedRecipient.description = this.getContactDescription(contact);
      }
    }

    // Actualizar opciones filtradas
    this.updateFilteredOptions();

    this.validateChannelCompatibility();
    this.updateEstimates();
  }

  // ✅ NUEVO: Método para filtrar opciones
  updateFilteredOptions(): void {
    this.filteredRecipientOptions = this.recipientOptions.filter(
      opt => opt.type === this.message.recipientType
    );
  }

  onChannelChange(): void {
    this.validateChannelCompatibility();
    this.updateEstimates();
    this.updateSubjectRequirement();
  }

  updateSubjectRequirement(): void {
    // Si es solo SMS, limpiar el subject
    if (this.message.channel === 'sms') {
      this.message.subject = '';
    }
    // Si es email o both y no tiene subject, poner uno por defecto
    else if ((this.message.channel === 'email' || this.message.channel === 'both') && !this.message.subject) {
      this.message.subject = 'Mensaje desde TFG App';
    }
  }

  validateChannelCompatibility(): void {
    if (!this.selectedRecipient) return;

    // Verificar compatibilidad del canal con el destinatario
    if (this.message.channel === 'email' && !this.selectedRecipient.hasEmail) {
      this.error = 'El destinatario seleccionado no tiene direcciones de email disponibles';
    } else if (this.message.channel === 'sms' && !this.selectedRecipient.hasPhone) {
      this.error = 'El destinatario seleccionado no tiene números de teléfono disponibles';
    } else {
      this.error = '';
    }
  }

  updateCharacterCount(): void {
    this.characterCount = this.message.content.length;
    this.updateEstimates();
  }

  updateEstimates(): void {
    if (!this.selectedRecipient) {
      this.estimatedRecipients = 0;
      this.estimatedCost = 0;
      return;
    }

    this.estimatedRecipients = this.selectedRecipient.contactCount || 0;
    this.estimatedCost = this.messageService.estimateMessageCost(this.message, this.estimatedRecipients);
  }

  togglePreview(): void {
    this.showPreview = !this.showPreview;
  }

  toggleScheduling(): void {
    this.showScheduling = !this.showScheduling;
    if (!this.showScheduling) {
      this.message.scheduledTime = '';
    }
  }

  isFormValid(): boolean {
    if (!this.message.recipientValue) return false;
    if (!this.message.content.trim()) return false;

    // Si es email, debe tener subject
    if ((this.message.channel === 'email' || this.message.channel === 'both') &&
      !this.message.subject?.trim()) {
      return false;
    }

    // Si está programado, debe tener fecha futura
    if (this.message.scheduledTime) {
      const scheduledDate = new Date(this.message.scheduledTime);
      const now = new Date();
      if (scheduledDate <= now) {
        return false;
      }
    }

    return true;
  }

  onSend(): void {
    if (!this.isFormValid()) {
      this.error = 'Por favor completa todos los campos obligatorios y verifica la información';
      return;
    }

    if (!this.messageService.isMessageValid(this.message)) {
      this.error = 'El mensaje no es válido. Verifica el contenido y destinatario';
      return;
    }

    this.sending = true;
    this.error = '';

    // ✅ NUEVO: Usar diferentes métodos según el tipo de destinatario
    let sendOperation: Observable<any>;

    switch (this.message.recipientType) {
      case 'individual':
        sendOperation = this.sendToIndividual();
        break;
      case 'category':
        sendOperation = this.sendToCategory();
        break;
      case 'all':
        sendOperation = this.sendToAll();
        break;
      default:
        this.error = 'Tipo de destinatario no válido';
        this.sending = false;
        return;
    }

    sendOperation.subscribe({
      next: (response) => {
        this.sending = false;
        if (response.success !== false) {
          this.success = true;
          this.showSuccessMessage(response);
          this.clearForm();
        } else {
          this.error = response.message || 'Error al enviar el mensaje';
        }
      },
      error: (error) => {
        console.error('Error enviando mensaje:', error);
        this.sending = false;
        this.error = error.error?.message || 'Error al enviar el mensaje. Intenta de nuevo.';
      }
    });
  }

  // ✅ NUEVOS: Métodos específicos para cada tipo de envío
  private sendToIndividual(): Observable<any> {
    const contactId = parseInt(this.message.recipientValue);
    const contact = this.contacts.find(c => c.id === contactId);

    console.log('📤 Enviando a contacto individual:', {
      contactId,
      contact,
      channel: this.message.channel,
      scheduled: !!this.message.scheduledTime
    });

    if (!contact) {
      throw new Error('Contacto no encontrado');
    }

    const apiUrl = 'http://localhost:8080/api'; // ✅ CORREGIDO: URL directa

    if (this.message.channel === 'email') {
      if (!contact.email) {
        throw new Error('El contacto no tiene email');
      }
      if (this.message.scheduledTime) {
        // Programar email
        const scheduleData = {
          to: contact.email,
          subject: this.message.subject || 'Mensaje desde TFG App',
          content: this.message.content,
          scheduledTime: this.message.scheduledTime
        };
        console.log('📅 Datos para programación:', scheduleData);
         // hello
        return this.http.post(`${apiUrl}/messaging/schedule`, scheduleData);
      } else {
        // Enviar email inmediato
        return this.messageService.sendEmail(contact.email, this.message.subject || 'Mensaje desde TFG App', this.message.content);
      }
    } else if (this.message.channel === 'sms') {
      if (!contact.phone) {
        throw new Error('El contacto no tiene teléfono');
      }
      // TODO: Implementar SMS programado si es necesario
      return this.messageService.sendSms(contact.phone, this.message.content);
    } else {
      throw new Error('Canal no soportado para contactos individuales');
    }
  }

  private sendToCategory(): Observable<any> {
    const categoryId = parseInt(this.message.recipientValue);
    return this.messageService.sendToCategory(
      categoryId,
      this.message.channel,
      this.message.subject || 'Mensaje desde TFG App',
      this.message.content
    );
  }

  private sendToAll(): Observable<any> {
    // TODO: Implementar envío a todos los contactos
    throw new Error('Envío a todos los contactos no implementado aún');
  }

  // ✅ NUEVO: Inyectar HttpClient para llamadas directas

  showSuccessMessage(response: any): void {
    let message = this.message.scheduledTime ?
      '⏰ Mensaje programado exitosamente' :
      '✅ Mensaje enviado exitosamente';

    if (response.totalRecipients) {
      message += ` a ${response.totalRecipients} destinatario(s)`;
    }

    if (response.messageId) {
      message += ` (ID: ${response.messageId})`;
    }

    // Mostrar mensaje de éxito por 5 segundos
    setTimeout(() => {
      this.success = false;
    }, 5000);
  }

  clearForm(): void {
    this.message = {
      recipientType: 'individual',
      recipientValue: '',
      channel: 'email',
      subject: '',
      content: '',
      scheduledTime: ''
    };
    this.selectedRecipient = null;
    this.characterCount = 0;
    this.estimatedCost = 0;
    this.estimatedRecipients = 0;
    this.showPreview = false;
    this.showScheduling = false;
    this.error = '';
  }

  // Métodos de utilidad para el template
  getChannelIcon(): string {
    return this.messageService.getChannelIcon(this.message.channel);
  }

  getChannelName(): string {
    return this.messageService.getChannelName(this.message.channel);
  }

  getRecipientSummary(): string {
    if (!this.selectedRecipient) return '';

    return `${this.selectedRecipient.label} - ${this.selectedRecipient.description}`;
  }

  getMinScheduleDateTime(): string {
    // Mínimo 5 minutos en el futuro
    const now = new Date();
    now.setMinutes(now.getMinutes() + 5);
    return now.toISOString().slice(0, 16); // Format: YYYY-MM-DDTHH:mm
  }

  // ✅ NUEVOS: Métodos para validaciones de canales
  isChannelDisabled(channel: string): boolean {
    if (!this.selectedRecipient) return false;

    switch (channel) {
      case 'email':
        return !this.selectedRecipient.hasEmail;
      case 'sms':
        return !this.selectedRecipient.hasPhone;
      case 'both':
        return !this.selectedRecipient.hasEmail || !this.selectedRecipient.hasPhone;
      default:
        return false;
    }
  }

  // ✅ NUEVO: Método de debug para verificar estado
  debugCurrentState(): void {
    console.log('🔍 ESTADO ACTUAL DEL FORMULARIO:', {
      message: this.message,
      selectedRecipient: this.selectedRecipient,
      contacts: this.contacts,
      recipientOptions: this.recipientOptions.filter(opt => opt.type === this.message.recipientType),
      isFormValid: this.isFormValid()
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }
}
