// src/main.ts debería ser algo así:
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import AppComponent from './app/app'; // ← Cambiado

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
