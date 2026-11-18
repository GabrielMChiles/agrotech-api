export interface FornecedorResponse {
  id: number;
  razaoSocial: string;
  cnpj: string;
  telefoneContato: string;
}

export interface ProdutoRequest {
  nome: string;
  categoria?: string | null;
  precoAtual: number;
  quantidadeEstoque: number;
  dataValidade?: string | null;
  fornecedorId: number;
}

export interface ProdutoResponse {
  id: number;
  nome: string;
  categoria: string | null;
  precoAtual: number;
  quantidadeEstoque: number;
  dataValidade: string | null;
  fornecedorId: number;
  nomeFornecedor: string;
}


export interface FornecedorRequest {
  razaoSocial: string;
  cnpj: string;
  telefoneContato?: string | null;
}
