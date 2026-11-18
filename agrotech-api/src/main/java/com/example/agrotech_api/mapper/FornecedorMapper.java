package com.example.agrotech_api.mapper;

import com.example.agrotech_api.model.Fornecedor;
import com.example.agrotech_api.model.dto.FornecedorRequestDTO;
import com.example.agrotech_api.model.dto.FornecedorResponseDTO;
import com.example.agrotech_api.validation.Documentos;
import org.springframework.stereotype.Component;

@Component
public class FornecedorMapper {
    public Fornecedor toEntity(FornecedorRequestDTO dto) {
        return Fornecedor.builder()
                .razaoSocial(dto.razaoSocial())
                .cnpj(Documentos.formatarCpfOuCnpj(dto.cnpj()))
                .telefoneContato(Documentos.formatarTelefone(dto.telefoneContato()))
                .build();
    }

    public FornecedorResponseDTO toResponseDTO(Fornecedor f) {
        return new FornecedorResponseDTO(f.getId(), f.getRazaoSocial(), f.getCnpj(), f.getTelefoneContato());
    }
}