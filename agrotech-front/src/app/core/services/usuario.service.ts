import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api.model';
import { FuncionarioResponse, UsuarioRequest, UsuarioResponse } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);

  listarTodos(): Observable<UsuarioResponse[]> {
    return this.http.get<UsuarioResponse[]>('/api/usuarios');
  }

  criar(payload: UsuarioRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>('/api/usuarios', payload);
  }

  listarFuncionarios(): Observable<PageResponse<FuncionarioResponse>> {
    return this.http.get<PageResponse<FuncionarioResponse>>('/api/funcionarios?size=100');
  }
}

