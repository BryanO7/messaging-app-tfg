// src/app/services/message.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Contact } from './contact.service'; // ✅ IMPORTAR Contact

export interface MessageRequest {
  // Destinatario
  recipientType: 'individual' | 'category' | 'all';
  recipientValue: string; // ID del contacto, ID de categoría, o 'all'

  // Canal y contenido
  channel: 'email' | 'sms' | 'both';
  subject?: string; // Solo para email
  content: string;

  // Programación (opcional)
  scheduledTime?: string; // ISO string

  // Adjuntos (para futuro)
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
  type: string; // EMAIL, SMS, BROADCAST
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

  constructor(private http: HttpClient) { }

  // === ENVÍO DE MENSAJES ===

  // Envío unificado (usa el endpoint principal del backend)
  sendMessage(messageData: MessageRequest): Observable<MessageResponse> {
    const endpoint = messageData.scheduledTime
      ? `${this.apiUrl}/messaging/schedule`
      : `${this.apiUrl}/messaging/send`;

    return this.http.post<MessageResponse>(endpoint, this.prepareMessageData(messageData));
  }

  // Envío de email individual
  sendEmail(to: string, subject: string, content: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/messaging/email`, {
      to,
      subject,
      content
    });
  }

  // Envío de SMS individual
  sendSms(to: string, content: string, sender?: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/messaging/sms`, {
      to,
      content,
      sender: sender || 'TFG-App'
    });
  }

  // Envío a categoría
  sendToCategory(categoryId: number, channel: string, subject: string, content: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/categories/${categoryId}/send-message`, {
      channel,
      subject,
      content
    });
  }

  // === HISTORIAL Y ESTADO ===

  // Obtener historial de mensajes
  getMessageHistory(days: number = 7, userId: string = 'currentUser'): Observable<any> {
    const params = new HttpParams()
      .set('days', days.toString())
      .set('userId', userId);

    return this.http.get<any>(`${this.apiUrl}/messages/history`, { params });
  }

  // Obtener mensajes por estado
  getMessagesByStatus(status: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/messages/by-status/${status}`);
  }

  // Obtener estado de un mensaje específico
  getMessageStatus(messageId: string): Observable<MessageStatus> {
    return this.http.get<MessageStatus>(`${this.apiUrl}/messages/${messageId}/status`);
  }

  // Reintentar mensaje fallido
  retryMessage(messageId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/messages/${messageId}/retry`, {});
  }

  // === ESTADÍSTICAS ===

  // Obtener estadísticas del sistema
  getSystemStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/messages/stats`);
  }

  // Obtener mensajes por destinatario
  getMessagesByRecipient(recipient: string): Observable<any> {
    const params = new HttpParams().set('recipient', recipient);
    return this.http.get<any>(`${this.apiUrl}/messages/by-recipient`, { params });
  }

  // === UTILIDADES PRIVADAS ===

  private prepareMessageData(messageData: MessageRequest): any {
    const prepared: any = {};

    // ✅ NUEVO: Configurar según el tipo de destinatario
    switch (messageData.recipientType) {
      case 'individual':
        // Para individual, necesitamos obtener el email/teléfono del contacto
        const contact = this.getContactById(parseInt(messageData.recipientValue));
        if (messageData.channel === 'email' && contact?.email) {
          prepared.to = contact.email;
        } else if (messageData.channel === 'sms' && contact?.phone) {
          prepared.to = contact.phone;
        }
        break;

      case 'category':
        // Para categorías, usar el endpoint específico de categorías
        prepared.categoryId = parseInt(messageData.recipientValue);
        prepared.channel = messageData.channel;
        break;

      case 'all':
        // Para todos, usar broadcast
        prepared.broadcast = true;
        prepared.channel = messageData.channel;
        break;
    }

    // Configurar contenido
    prepared.content = messageData.content;

    // Añadir subject si es email
    if (messageData.channel === 'email' || messageData.channel === 'both') {
      prepared.subject = messageData.subject || 'Mensaje desde TFG App';
    }

    // Añadir fecha programada si existe
    if (messageData.scheduledTime) {
      prepared.scheduledTime = messageData.scheduledTime;
    }

    return prepared;
  }

  // ✅ NUEVO: Cache simple de contactos para obtener email/teléfono
  private contactCache: Contact[] = [];

  setContactCache(contacts: Contact[]): void {
    this.contactCache = contacts;
  }

  private getContactById(id: number): Contact | undefined {
    return this.contactCache.find(c => c.id === id);
  }

  // === VALIDACIONES ===

  // Validar que el mensaje tenga contenido válido
  isMessageValid(messageData: MessageRequest): boolean {
    if (!messageData.content.trim()) return false;
    if (!messageData.recipientValue) return false;

    // Si es email, debe tener subject
    if ((messageData.channel === 'email' || messageData.channel === 'both') &&
      !messageData.subject?.trim()) {
      return false;
    }

    return true;
  }

  // Estimar costo del mensaje
  estimateMessageCost(messageData: MessageRequest, recipientCount: number): number {
    let costPerMessage = 0;

    switch (messageData.channel) {
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

  // Obtener icono del canal
  getChannelIcon(channel: string): string {
    const icons = {
      email: '📧',
      sms: '📱',
      both: '📧📱'
    };
    return icons[channel as keyof typeof icons] || '📧';
  }

  // Obtener nombre legible del canal
  getChannelName(channel: string): string {
    const names = {
      email: 'Email',
      sms: 'SMS',
      both: 'Email + SMS'
    };
    return names[channel as keyof typeof names] || 'Email';
  }
}
