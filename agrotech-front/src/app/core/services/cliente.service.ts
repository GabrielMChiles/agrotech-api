import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api.model';
import { ClienteRequest, ClienteResponse } from '../models/cliente.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);

  listar(size = 100): Observable<PageResponse<ClienteResponse>> {
    return this.http.get<PageResponse<ClienteResponse>>(`/api/clientes?size=${size}`);
  }

  criar(payload: ClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>('/api/clientes', payload);
  }

  atualizar(id: number, payload: ClienteRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`/api/clientes/${id}`, payload);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`/api/clientes/${id}`);
  }
}

