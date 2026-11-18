package com.example.agrotech_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TB_PRODUTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 60)
    private String categoria;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal precoAtual;

    @Column(nullable = false)
    private Integer quantidadeEstoque;

    private LocalDate dataValidade;

    // Bloqueio otimista: impede duas vendas simultâneas de baixarem o mesmo saldo
    @Version
    @Column(nullable = false, columnDefinition = "bigint not null default 0")
    private long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;
}
