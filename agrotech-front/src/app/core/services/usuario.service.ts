import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api.model';
import {
  FuncionarioRequest,
  FuncionarioResponse,
  UsuarioRequest,
  UsuarioResponse,
  UsuarioUpdateRequest
} from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);

  listarTodos(): Observable<UsuarioResponse[]> {
    return this.http.get<UsuarioResponse[]>('/api/usuarios');
  }

  criar(payload: UsuarioRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>('/api/usuarios', payload);
  }

  atualizar(id: number, payload: UsuarioUpdateRequest): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(`/api/usuarios/${id}`, payload);
  }

  listarFuncionarios(): Observable<PageResponse<FuncionarioResponse>> {
    return this.http.get<PageResponse<FuncionarioResponse>>('/api/funcionarios?size=100');
  }

  criarFuncionario(payload: FuncionarioRequest): Observable<FuncionarioResponse> {
    return this.http.post<FuncionarioResponse>('/api/funcionarios', payload);
  }

  atualizarFuncionario(id: number, payload: FuncionarioRequest): Observable<FuncionarioResponse> {
    return this.http.put<FuncionarioResponse>(`/api/funcionarios/${id}`, payload);
  }
}

