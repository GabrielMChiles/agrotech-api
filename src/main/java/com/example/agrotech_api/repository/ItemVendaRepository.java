package com.example.agrotech_api.repository;

import com.example.agrotech_api.model.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {
}