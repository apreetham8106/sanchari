import { HttpErrorResponse } from '@angular/common/http';

/** Turns a backend ApiError (or a network failure) into one readable line. */
export function errorMessage(err: unknown, fallback = 'Something went wrong'): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'Cannot reach the server. Check your connection.';
    }
    const body = err.error;
    if (body && typeof body === 'object') {
      const fields = body.fieldErrors as Record<string, string> | undefined;
      if (fields && Object.keys(fields).length) {
        return Object.values(fields)[0];
      }
      if (typeof body.message === 'string' && body.message) {
        return body.message;
      }
    }
    return fallback;
  }
  return fallback;
}
