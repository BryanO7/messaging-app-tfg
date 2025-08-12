import { Routes } from '@angular/router';
import { ContactListComponent } from './components/contact-list/contact-list.component';

export const routes: Routes = [
  { path: '', redirectTo: '/contacts', pathMatch: 'full' },
  { path: 'contacts', component: ContactListComponent }
];
