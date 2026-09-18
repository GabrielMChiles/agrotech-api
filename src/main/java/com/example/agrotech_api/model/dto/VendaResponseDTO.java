package com.example.agrotech_api.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponseDTO(
        Long id,
        LocalDateTime dataHoraVenda,
        BigDecimal valorTotal,
        String status,
        String nomeCliente,
        String nomeFuncionario,
        List<ItemVendaResponseDTO> itens
) {}
