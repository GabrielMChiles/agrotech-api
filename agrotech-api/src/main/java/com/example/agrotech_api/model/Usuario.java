package com.example.agrotech_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_USUARIO")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false, length = 120)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleUsuario role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id", nullable = false, unique = true)
    private Funcionario funcionario;
}

