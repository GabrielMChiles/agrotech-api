package com.example.agrotech_api.repository;

import com.example.agrotech_api.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    @Query("SELECT v FROM Venda v " +
            "JOIN FETCH v.cliente c " +
            "JOIN FETCH v.funcionario f " +
            "JOIN FETCH v.itens i " +
            "JOIN FETCH i.produto p " +
            "WHERE v.id = :id")
    Optional<Venda> findByIdComDetalhesCompletos(@Param("id") Long id);

    @Query("SELECT DISTINCT v FROM Venda v " +
            "JOIN FETCH v.cliente c " +
            "JOIN FETCH v.funcionario f " +
            "JOIN FETCH v.itens i " +
            "JOIN FETCH i.produto p " +
            "ORDER BY v.dataHoraVenda DESC")
    List<Venda> findAllComDetalhesCompletos();
}
