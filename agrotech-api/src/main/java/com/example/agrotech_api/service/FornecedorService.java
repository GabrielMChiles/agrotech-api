package com.example.agrotech_api.service;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.exception.ResourceNotFoundException;
import com.example.agrotech_api.model.Fornecedor;
import com.example.agrotech_api.repository.FornecedorRepository;
import com.example.agrotech_api.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository, ProdutoRepository produtoRepository) {
        this.fornecedorRepository = fornecedorRepository;
        this.produtoRepository = produtoRepository;
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
        fornecedorRepository.findByCnpj(fornecedorAtualizado.getCnpj())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new BusinessRuleException(
                            "Já existe um fornecedor cadastrado com o CNPJ: " + fornecedorAtualizado.getCnpj());
                });
        fornecedorExistente.setCnpj(fornecedorAtualizado.getCnpj());
        fornecedorExistente.setRazaoSocial(fornecedorAtualizado.getRazaoSocial());
        fornecedorExistente.setTelefoneContato(fornecedorAtualizado.getTelefoneContato());
        return fornecedorRepository.save(fornecedorExistente);
    }

    @Transactional
    public void excluir(Long id) {
        Fornecedor fornecedor = buscarPorId(id);
        long produtosVinculados = produtoRepository.countByFornecedorId(id);
        if (produtosVinculados > 0) {
            throw new BusinessRuleException("O fornecedor '" + fornecedor.getRazaoSocial() + "' possui "
                    + produtosVinculados + " produto(s) vinculado(s). Troque o fornecedor desses produtos antes de excluí-lo.");
        }
        fornecedorRepository.delete(fornecedor);
    }

    public Fornecedor buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado com o ID: " + id));
    }

    public Page<Fornecedor> listarTodos(Pageable pageable) {
        return fornecedorRepository.findAll(pageable);
    }
}