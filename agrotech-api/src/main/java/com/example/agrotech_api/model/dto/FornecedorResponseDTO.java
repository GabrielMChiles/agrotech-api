package com.example.agrotech_api.model.dto;

public record FornecedorResponseDTO(
        Long id, String razaoSocial, String cnpj, String telefoneContato
) {}