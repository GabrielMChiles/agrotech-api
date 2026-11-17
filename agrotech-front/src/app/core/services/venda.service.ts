import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { VendaRequest, VendaResponse } from '../models/venda.model';

@Injectable({ providedIn: 'root' })
export class VendaService {
  private readonly http = inject(HttpClient);

  listarTodas(): Observable<VendaResponse[]> {
    return this.http.get<VendaResponse[]>('/api/vendas');
  }

  buscarPorId(id: number): Observable<VendaResponse> {
    return this.http.get<VendaResponse>(`/api/vendas/${id}`);
  }

  efetivar(payload: VendaRequest): Observable<VendaResponse> {
    return this.http.post<VendaResponse>('/api/vendas', payload);
  }

  cancelar(id: number): Observable<void> {
    return this.http.patch<void>(`/api/vendas/${id}/cancelar`, {});
  }
}

