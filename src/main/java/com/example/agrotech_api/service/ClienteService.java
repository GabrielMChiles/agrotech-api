package com.example.agrotech_api.service;


import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.exception.ResourceNotFoundException;
import com.example.agrotech_api.model.Cliente;
import com.example.agrotech_api.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Cliente salvar(Cliente cliente) {
        if (clienteRepository.findByCpfCnpj(cliente.getCpfCnpj()).isPresent()) {
            throw new BusinessRuleException("Já existe um cliente cadastrado com o CPF/CNPJ: " + cliente.getCpfCnpj());
        }
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Long id, Cliente clienteAtualizado) {
        Cliente clienteExistente = buscarPorId(id);
        clienteExistente.setNome(clienteAtualizado.getNome());
        clienteExistente.setInscricaoEstadual(clienteAtualizado.getInscricaoEstadual());
        return clienteRepository.save(clienteExistente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
    }

    public Page<Cliente> listarTodos(Pageable pageable) {
        return clienteRepository.findAll(pageable);
    }
}