package com.example.agrotech_api.model.dto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(
        Long id, String nome, BigDecimal precoAtual, Integer quantidadeEstoque, Long fornecedorId, String nomeFornecedor
) {}