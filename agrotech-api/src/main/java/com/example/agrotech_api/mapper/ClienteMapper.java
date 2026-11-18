package com.example.agrotech_api.mapper;

import com.example.agrotech_api.model.Cliente;
import com.example.agrotech_api.model.dto.ClienteRequestDTO;
import com.example.agrotech_api.model.dto.ClienteResponseDTO;
import com.example.agrotech_api.validation.Documentos;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    public Cliente toEntity(ClienteRequestDTO dto) {
        return Cliente.builder()
                .nome(dto.nome())
                .cpfCnpj(Documentos.formatarCpfOuCnpj(dto.cpfCnpj()))
                .inscricaoEstadual(dto.inscricaoEstadual())
                .telefone(Documentos.formatarTelefone(dto.telefone()))
                .endereco(dto.endereco())
                .build();
    }

    public ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getInscricaoEstadual(),
                cliente.getTelefone(),
                cliente.getEndereco()
        );
    }
}
