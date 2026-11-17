import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api.model';
import { FornecedorResponse, ProdutoRequest, ProdutoResponse } from '../models/produto.model';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private readonly http = inject(HttpClient);

  listar(size = 100): Observable<PageResponse<ProdutoResponse>> {
    return this.http.get<PageResponse<ProdutoResponse>>(`/api/produtos?size=${size}`);
  }

  criar(payload: ProdutoRequest): Observable<ProdutoResponse> {
    return this.http.post<ProdutoResponse>('/api/produtos', payload);
  }

  atualizar(id: number, payload: ProdutoRequest): Observable<ProdutoResponse> {
    return this.http.put<ProdutoResponse>(`/api/produtos/${id}`, payload);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`/api/produtos/${id}`);
  }

  listarFornecedores(): Observable<PageResponse<FornecedorResponse>> {
    return this.http.get<PageResponse<FornecedorResponse>>('/api/fornecedores?size=100');
  }
}

