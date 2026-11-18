import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api.model';
import { FornecedorRequest, FornecedorResponse } from '../models/produto.model';

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private readonly http = inject(HttpClient);

  listar(size = 100): Observable<PageResponse<FornecedorResponse>> {
    return this.http.get<PageResponse<FornecedorResponse>>(`/api/fornecedores?size=${size}`);
  }

  criar(payload: FornecedorRequest): Observable<FornecedorResponse> {
    return this.http.post<FornecedorResponse>('/api/fornecedores', payload);
  }

  atualizar(id: number, payload: FornecedorRequest): Observable<FornecedorResponse> {
    return this.http.put<FornecedorResponse>(`/api/fornecedores/${id}`, payload);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`/api/fornecedores/${id}`);
  }
}
