package com.example.agrotech_api.repository;

import com.example.agrotech_api.model.Fornecedor;
import com.example.agrotech_api.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    // REGRA 4.3: Uso obrigatório de JOIN FETCH para evitar o N+1 Problem.
    // Se você carregar uma Venda e depois tentar acessar getItens(), getCliente() ou getFuncionario()
    // com fetch LAZY, o Hibernate fará 4 queries no banco. Com JOIN FETCH, fazemos tudo em 1 query.
    @Query("SELECT v FROM Venda v " +
            "JOIN FETCH v.cliente c " +
            "JOIN FETCH v.funcionario f " +
            "JOIN FETCH v.itens i " +
            "JOIN FETCH i.produto p " +
            "WHERE v.id = :id")
    Optional<Venda> findByIdComDetalhesCompletos(@Param("id") Long id);
}