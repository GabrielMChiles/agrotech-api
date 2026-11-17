package com.example.agrotech_api.repository;

import com.example.agrotech_api.model.Fornecedor;
import com.example.agrotech_api.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Exemplo de query derivada: Buscar produtos com estoque baixo
    List<Produto> findByQuantidadeEstoqueLessThan(Integer quantidade);

    @Override
    @EntityGraph(attributePaths = "fornecedor")
    Page<Produto> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "fornecedor")
    Optional<Produto> findById(Long id);
}