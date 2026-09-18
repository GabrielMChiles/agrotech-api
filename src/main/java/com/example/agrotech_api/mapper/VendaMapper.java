package com.example.agrotech_api.mapper;

import com.example.agrotech_api.model.dto.*;
import com.example.agrotech_api.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class VendaMapper {

    public Venda toEntity(VendaRequestDTO dto) {
        Cliente cliente = Cliente.builder().id(dto.clienteId()).build();
        Funcionario funcionario = Funcionario.builder().id(dto.funcionarioId()).build();

        List<ItemVenda> itens = dto.itens().stream().map(itemDto ->
                ItemVenda.builder()
                        .produto(Produto.builder().id(itemDto.produtoId()).build())
                        .quantidade(itemDto.quantidade())
                        .build()
        ).toList();

        Venda venda = Venda.builder()
                .cliente(cliente)
                .funcionario(funcionario)
                .build();

        itens.forEach(venda::addItemVenda);
        return venda;
    }

    public VendaResponseDTO toResponseDTO(Venda venda) {
        List<ItemVendaResponseDTO> itensDTO = venda.getItens().stream().map(item ->
                new ItemVendaResponseDTO(
                        item.getProduto().getId(),
                        item.getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitarioNoMomentoDaVenda(),
                        item.getPrecoUnitarioNoMomentoDaVenda().multiply(BigDecimal.valueOf(item.getQuantidade()))
                )
        ).toList();

        return new VendaResponseDTO(
                venda.getId(),
                venda.getDataHoraVenda(),
                venda.getValorTotal(),
                venda.getStatus().name(),
                venda.getCliente().getNome(),
                venda.getFuncionario().getNome(),
                itensDTO
        );
    }
}