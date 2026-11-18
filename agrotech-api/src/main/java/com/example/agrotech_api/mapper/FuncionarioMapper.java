package com.example.agrotech_api.mapper;

import com.example.agrotech_api.model.Funcionario;
import com.example.agrotech_api.model.dto.FuncionarioRequestDTO;
import com.example.agrotech_api.model.dto.FuncionarioResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioMapper {
    public Funcionario toEntity(FuncionarioRequestDTO dto) {
        return Funcionario.builder()
                .nome(dto.nome())
                .matricula(dto.matricula().trim().toUpperCase())
                .cargo(dto.cargo())
                .build();
    }

    public FuncionarioResponseDTO toResponseDTO(Funcionario f) {
        return new FuncionarioResponseDTO(f.getId(), f.getNome(), f.getMatricula(), f.getCargo());
    }
}