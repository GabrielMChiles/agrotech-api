import { RoleUsuario } from './auth.model';

export interface UsuarioRequest {
  username: string;
  senha: string;
  role: RoleUsuario;
  funcionarioId: number;
}

export interface UsuarioResponse {
  id: number;
  username: string;
  role: RoleUsuario;
  funcionarioId: number;
  funcionarioNome: string;
  funcionarioMatricula: string;
}

export interface FuncionarioResponse {
  id: number;
  nome: string;
  matricula: string;
  cargo: string;
}

