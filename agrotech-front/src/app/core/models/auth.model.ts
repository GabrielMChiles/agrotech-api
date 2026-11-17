export type RoleUsuario = 'ADMIN' | 'FUNCIONARIO';

export interface LoginRequest {
  username: string;
  senha: string;
}

export interface UserSession {
  token: string;
  username: string;
  role: RoleUsuario;
  funcionarioId: number;
  funcionarioNome: string;
}

