import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  const request = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(request).pipe(
    catchError((err: unknown) => {
      // Token rejeitado pelo servidor (expirado ou servidor reiniciado): encerra a sessão local
      if (err instanceof HttpErrorResponse && err.status === 401 && token && !req.url.includes('/api/auth/login')) {
        auth.logout();
      }
      return throwError(() => err);
    })
  );
};
