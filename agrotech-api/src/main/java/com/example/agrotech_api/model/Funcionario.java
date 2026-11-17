package com.example.agrotech_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_FUNCIONARIO")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Funcionario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String matricula;

    @Column(nullable = false, length = 50)
    private String cargo;
}