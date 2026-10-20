import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { AuthService } from './auth.service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  if (!token) {
    return next(request);
  }

  const authorizedRequest = request.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });

  return next(authorizedRequest).pipe(catchError((error: unknown) => {
    if (error instanceof HttpErrorResponse
      && error.status === 401
      && !request.url.includes('/api/v1/auth/token')) {
      auth.expireSession();
    }
    return throwError(() => error);
  }));
};