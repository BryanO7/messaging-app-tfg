// src/app/services/message.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Contact } from './contact.service';

export interface MessageRequest {
  recipientType: 'individual' | 'category' | 'all' | 'multiple' | '';
  recipientValue: string;
  channel: 'email' | 'sms' | 'both' | '';
  subject?: string;
  content: string;
  scheduledTime?: string;
  attachments?: string[];
}

export interface MessageResponse {
  success: boolean;
  message: string;
  messageId?: string;
  scheduledTime?: string;
  totalRecipients?: number;
  emailRecipients?: number;
  smsRecipients?: number;
}

export interface MessageStatus {
  id?: number;
  messageId: string;
  recipient: string;
  type: string;
  status: 'QUEUED' | 'PROCESSING' | 'SENT' | 'DELIVERED' | 'FAILED' | 'SCHEDULED' | 'CANCELLED';
  timestamp: string;
  subject?: string;
  content: string;
  errorMessage?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private apiUrl = 'http://localhost:8080/api';
  private contactCache: Contact[] = [];

  constructor(private http: HttpClient) { }

  // === CACHE DE CONTACTOS ===
  setContactCache(contacts: Contact[]): void {
    this.contactCache = contacts;
  }

  private getContactById(id: number): Contact | undefined {
    return this.contactCache.find(c => c.id === id);
  }

  // === ENVÍO DE MENSAJES ===

  // Envío unificado
  sendMessage(messageData: MessageRequest): Observable<MessageResponse> {
    console.log('🚀 MessageService.sendMessage llamado con:', messageData);

    const endpoint = messageData.scheduledTime
      ? `${this.apiUrl}/messaging/schedule`
      : `${this.apiUrl}/messaging/send`;

    const preparedData = this.prepareMessageData(messageData);
    console.log('📤 Enviando a:', endpoint, preparedData);

    return this.http.post<MessageResponse>(endpoint, preparedData);
  }

  // Envío de email individual
  sendEmail(to: string, subject: string, content: string): Observable<MessageResponse> {
    console.log('📧 Enviando email directo a:', to);
    return this.http.post<MessageResponse>(`${this.apiUrl}/messaging/email`, {
      to,
      subject,
      content
    });
  }

  // Envío de SMS individual
  sendSms(to: string, content: string, sender?: string): Observable<MessageResponse> {
    console.log('📱 Enviando SMS directo a:', to);
    return this.http.post<MessageResponse>(`${this.apiUrl}/messaging/sms`, {
      to,
      content,
      sender: sender || 'TFG-App'
    });
  }

  // Envío a categoría
  sendToCategory(categoryId: number, channel: string, subject: string, content: string): Observable<MessageResponse> {
    console.log('📁 Enviando a categoría:', categoryId);
    return this.http.post<MessageResponse>(`${this.apiUrl}/categories/${categoryId}/send-message`, {
      channel,
      subject,
      content
    });
  }

  // === HISTORIAL Y ESTADO ===

  getMessageHistory(days: number = 7, userId: string = 'currentUser'): Observable<any> {
    const params = new HttpParams()
      .set('days', days.toString())
      .set('userId', userId);

    return this.http.get<any>(`${this.apiUrl}/messages/history`, { params });
  }

  getMessagesByStatus(status: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/messages/by-status/${status}`);
  }

  getMessageStatus(messageId: string): Observable<MessageStatus> {
    return this.http.get<MessageStatus>(`${this.apiUrl}/messages/${messageId}/status`);
  }

  retryMessage(messageId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/messages/${messageId}/retry`, {});
  }

  getSystemStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/messages/stats`);
  }

  getMessagesByRecipient(recipient: string): Observable<any> {
    const params = new HttpParams().set('recipient', recipient);
    return this.http.get<any>(`${this.apiUrl}/messages/by-recipient`, { params });
  }

  // === UTILIDADES PRIVADAS ===

  private prepareMessageData(messageData: MessageRequest): any {
    console.log('🔧 Preparando datos para:', messageData);

    const prepared: any = {
      content: messageData.content,
      channel: messageData.channel  // ✅ CRITICAL: Enviar el campo channel
    };

    // Configurar según el tipo de destinatario
    switch (messageData.recipientType) {
      case 'individual':
        const contact = this.getContactById(parseInt(messageData.recipientValue));
        console.log('👤 Contacto encontrado:', contact);
        console.log('🔧 Canal seleccionado:', messageData.channel);

        // ✅ CORREGIDO: Enviar destinatario correcto según el canal
        if (messageData.channel === 'email' && contact?.email) {
          prepared.to = contact.email;
          console.log('📧 Enviando a email:', contact.email);
        } else if (messageData.channel === 'sms' && contact?.phone) {
          prepared.to = contact.phone;
          console.log('📱 Enviando a teléfono:', contact.phone);
        } else if (messageData.channel === 'both' && contact) {
          // ✅ NUEVO: Para 'both', enviar campos separados
          prepared.to = contact.email || contact.phone; // Fallback
          prepared.email = contact.email;
          prepared.phone = contact.phone;
          console.log('📧📱 Enviando a ambos - Email:', contact.email, 'Phone:', contact.phone);
        } else {
          console.error('❌ No se encontró destinatario válido para el canal:', messageData.channel);
        }
        break;

      case 'category':
        prepared.categoryId = parseInt(messageData.recipientValue);
        break;

      case 'all':
        prepared.broadcast = true;
        break;

      case 'multiple':
        // ✅ NUEVO: Manejar múltiples contactos
        prepared.recipients = messageData.recipientValue.split(',').map(id => parseInt(id.trim()));
        prepared.broadcast = true;
        break;
    }

    // Añadir subject si es email
    if (messageData.channel === 'email' || messageData.channel === 'both') {
      prepared.subject = messageData.subject || 'Mensaje desde TFG App';
    }

    // Añadir sender para SMS
    if (messageData.channel === 'sms' || messageData.channel === 'both') {
      prepared.sender = 'TFG-App';
    }

    // Añadir fecha programada si existe
    if (messageData.scheduledTime) {
      prepared.scheduledTime = messageData.scheduledTime;
    }

    console.log('✅ Datos preparados finales:', prepared);
    return prepared;
  }

  // === VALIDACIONES ===

  isMessageValid(messageData: MessageRequest): boolean {
    if (!messageData.content.trim()) {
      console.log('❌ Validación falló: contenido vacío');
      return false;
    }
    if (!messageData.recipientValue) {
      console.log('❌ Validación falló: destinatario vacío');
      return false;
    }

    // Si es email, debe tener subject
    if ((messageData.channel === 'email' || messageData.channel === 'both') &&
      !messageData.subject?.trim()) {
      console.log('❌ Validación falló: subject vacío para email');
      return false;
    }

    console.log('✅ Validación exitosa');
    return true;
  }

  estimateMessageCost(messageData: MessageRequest, recipientCount: number): number {
    let costPerMessage = 0;

    switch (messageData.channel) {
      case '':
        costPerMessage = 0;
        break;
      case 'email':
        costPerMessage = 0.01; // 1 céntimo por email
        break;
      case 'sms':
        costPerMessage = 0.05; // 5 céntimos por SMS
        break;
      case 'both':
        costPerMessage = 0.06; // Email + SMS
        break;
    }

    return recipientCount * costPerMessage;
  }

  getChannelIcon(channel: string): string {
    const icons = {
      email: '📧',
      sms: '📱',
      both: '📧📱'
    };
    return icons[channel as keyof typeof icons] || '📧';
  }

  getChannelName(channel: string): string {
    const names = {
      email: 'Email',
      sms: 'SMS',
      both: 'Email + SMS'
    };
    return names[channel as keyof typeof names] || 'Email';
  }
}
