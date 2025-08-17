// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { ContactListComponent } from './components/contact-list/contact-list.component';
import { ContactFormComponent } from './components/contact-form/contact-form.component';
import { CategoryListComponent } from './components/category-list/category-list.component';
import { CategoryFormComponent } from './components/category-form/category-form.component';
import { MessageFormComponent } from './components/message-form/message-form.component';
import { CategoryDetailComponent } from './components/category-detail/category-detail.component'
export const routes: Routes = [
  { path: '', redirectTo: '/contacts', pathMatch: 'full' },

  // Rutas de Contactos
  { path: 'contacts', component: ContactListComponent },
  { path: 'contacts/new', component: ContactFormComponent },
  { path: 'contacts/:id/edit', component: ContactFormComponent },

  // Rutas de Categorías
  { path: 'categories', component: CategoryListComponent },
  { path: 'categories/new', component: CategoryFormComponent },
  { path: 'categories/:id/edit', component: CategoryFormComponent },

  // Rutas de Mensajería
  { path: 'messages', component: MessageFormComponent },
  { path: 'messages/new', component: MessageFormComponent },

  // TODO: Implementar más adelante
  // { path: 'messages/history', component: MessageHistoryComponent },
  // { path: 'dashboard', component: DashboardComponent },
  {
    path: 'categories/:id',
    component: CategoryDetailComponent
  },

  { path: '**', redirectTo: '/contacts' } // Redirigir rutas no encontradas
];
