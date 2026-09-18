package com.example.agrotech_api.service;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.exception.ResourceNotFoundException;
import com.example.agrotech_api.model.Fornecedor;
import com.example.agrotech_api.repository.FornecedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public Fornecedor salvar(Fornecedor fornecedor) {
        if (fornecedorRepository.findByCnpj(fornecedor.getCnpj()).isPresent()) {
            throw new BusinessRuleException("Já existe um fornecedor cadastrado com o CNPJ: " + fornecedor.getCnpj());
        }
        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public Fornecedor atualizar(Long id, Fornecedor fornecedorAtualizado) {
        Fornecedor fornecedorExistente = buscarPorId(id);
        fornecedorExistente.setRazaoSocial(fornecedorAtualizado.getRazaoSocial());
        fornecedorExistente.setTelefoneContato(fornecedorAtualizado.getTelefoneContato());
        return fornecedorRepository.save(fornecedorExistente);
    }

    public Fornecedor buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado com o ID: " + id));
    }

    public Page<Fornecedor> listarTodos(Pageable pageable) {
        return fornecedorRepository.findAll(pageable);
    }
}