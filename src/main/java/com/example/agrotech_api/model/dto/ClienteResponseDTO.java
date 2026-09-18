package com.example.agrotech_api.model.dto;

public record ClienteResponseDTO(
        Long id, String nome, String cpfCnpj, String inscricaoEstadual
) {}