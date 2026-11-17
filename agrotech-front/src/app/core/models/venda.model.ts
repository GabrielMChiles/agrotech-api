export interface ItemVendaRequest {
  produtoId: number;
  quantidade: number;
}

export interface VendaRequest {
  clienteId: number;
  funcionarioId: number;
  itens: ItemVendaRequest[];
}

export interface ItemVendaResponse {
  produtoId: number;
  nomeProduto: string;
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
}

export interface VendaResponse {
  id: number;
  dataHoraVenda: string;
  valorTotal: number;
  status: 'CONCLUIDA' | 'CANCELADA';
  nomeCliente: string;
  nomeFuncionario: string;
  itens: ItemVendaResponse[];
}

export interface CartItem {
  produtoId: number;
  nomeProduto: string;
  precoUnitario: number;
  quantidade: number;
  estoqueDisponivel: number;
}

