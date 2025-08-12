// src/app/services/contact.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Contact {
  id?: number;
  name: string;
  email?: string;
  phone?: string;
  whatsappId?: string;
  notes?: string;
}

export interface ContactRequest {
  name: string;
  email?: string;
  phone?: string;
  whatsappId?: string;
  notes?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private apiUrl = 'http://localhost:8080/api/contacts';

  constructor(private http: HttpClient) { }

  // Obtener todos los contactos
  getAllContacts(): Observable<Contact[]> {
    return this.http.get<Contact[]>(this.apiUrl);
  }

  // Obtener contacto por ID
  getContactById(id: number): Observable<Contact> {
    return this.http.get<Contact>(`${this.apiUrl}/${id}`);
  }

  // Crear nuevo contacto
  createContact(contact: ContactRequest): Observable<any> {
    return this.http.post<any>(this.apiUrl, contact);
  }

  // Actualizar contacto
  updateContact(id: number, contact: ContactRequest): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, contact);
  }

  // Eliminar contacto
  deleteContact(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  // Buscar contactos
  searchContacts(query: string): Observable<Contact[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<Contact[]>(`${this.apiUrl}/search`, { params });
  }
}
