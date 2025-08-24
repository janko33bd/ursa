import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpInterceptorFn } from '@angular/common/http';

import { routes } from './app.routes';

// Functional JWT Interceptor
const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const currentUser = localStorage.getItem('currentUser');
  
  // Apply JWT to API requests (check for api path instead of hostname)
  if (currentUser && req.url.includes('/api/')) {
    try {
      const user = JSON.parse(currentUser);
      if (user.token) {
        const authReq = req.clone({
          setHeaders: {
            Authorization: `Bearer ${user.token}`
          }
        });
        return next(authReq);
      }
    } catch (e) {
      // Silent error handling
    }
  }
  
  return next(req);
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }), 
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor]))
  ]
};
