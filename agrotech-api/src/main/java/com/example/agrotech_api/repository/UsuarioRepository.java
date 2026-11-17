package com.example.agrotech_api.repository;

import com.example.agrotech_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u JOIN FETCH u.funcionario WHERE u.username = :username")
    Optional<Usuario> findByUsernameComFuncionario(@Param("username") String username);

    boolean existsByUsername(String username);

    boolean existsByFuncionarioId(Long funcionarioId);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.funcionario ORDER BY u.id ASC")
    List<Usuario> findAllComFuncionario();
}

