import { HttpErrorResponse } from '@angular/common/http';
import { ProblemDetail } from '../models/api.model';

export function extractApiErrorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const problem = err.error as ProblemDetail | undefined;
    if (problem?.erros && problem.erros.length > 0) {
      return problem.erros.join(' | ');
    }
    if (problem?.detail) {
      return problem.detail;
    }
    if (err.status === 0) {
      return 'Não foi possível conectar ao servidor da API.';
    }
  }
  return 'Erro inesperado no servidor.';
}

