package com.example.agrotech_api.service;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.exception.ResourceNotFoundException;
import com.example.agrotech_api.model.Funcionario;
import com.example.agrotech_api.repository.FuncionarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional
    public Funcionario salvar(Funcionario funcionario) {
        if (funcionarioRepository.findByMatricula(funcionario.getMatricula()).isPresent()) {
            throw new BusinessRuleException("Já existe um funcionário cadastrado com a matrícula: " + funcionario.getMatricula());
        }
        return funcionarioRepository.save(funcionario);
    }

    @Transactional
    public Funcionario atualizar(Long id, Funcionario funcionarioAtualizado) {
        Funcionario funcionarioExistente = buscarPorId(id);
        funcionarioExistente.setNome(funcionarioAtualizado.getNome());
        funcionarioExistente.setCargo(funcionarioAtualizado.getCargo());
        return funcionarioRepository.save(funcionarioExistente);
    }

    public Funcionario buscarPorId(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com o ID: " + id));
    }

    public Page<Funcionario> listarTodos(Pageable pageable) {
        return funcionarioRepository.findAll(pageable);
    }
}