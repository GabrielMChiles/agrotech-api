package com.example.agrotech_api.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String categoria,
        BigDecimal precoAtual,
        Integer quantidadeEstoque,
        LocalDate dataValidade,
        Long fornecedorId,
        String nomeFornecedor
) {}
