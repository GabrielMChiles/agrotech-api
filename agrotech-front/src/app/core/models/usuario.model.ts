import { RoleUsuario } from './auth.model';

export interface UsuarioRequest {
  username: string;
  senha: string;
  role: RoleUsuario;
  funcionarioId: number;
}

export interface UsuarioUpdateRequest {
  username: string;
  senha?: string | null;
  role: RoleUsuario;
}

export interface UsuarioResponse {
  id: number;
  username: string;
  role: RoleUsuario;
  funcionarioId: number;
  funcionarioNome: string;
  funcionarioMatricula: string;
}

export interface FuncionarioRequest {
  nome: string;
  matricula: string;
  cargo: string;
}

export interface FuncionarioResponse {
  id: number;
  nome: string;
  matricula: string;
  cargo: string;
}

