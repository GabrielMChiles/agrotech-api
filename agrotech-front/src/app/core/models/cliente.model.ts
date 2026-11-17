export interface ClienteRequest {
  nome: string;
  cpfCnpj: string;
  inscricaoEstadual?: string | null;
  telefone?: string | null;
  endereco?: string | null;
}

export interface ClienteResponse {
  id: number;
  nome: string;
  cpfCnpj: string;
  inscricaoEstadual: string | null;
  telefone: string | null;
  endereco: string | null;
}

