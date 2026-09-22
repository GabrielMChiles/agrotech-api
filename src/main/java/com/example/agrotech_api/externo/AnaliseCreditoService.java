package com.example.agrotech_api.externo;

import com.example.agrotech_api.exception.BusinessRuleException;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class AnaliseCreditoService {

    private final AnaliseCreditoGateway creditoGateway;

    public AnaliseCreditoService(AnaliseCreditoGateway creditoGateway) {
        this.creditoGateway = creditoGateway;
    }

    public void validarCreditoSafra(String cpfCnpj, BigDecimal valorVenda) {
        boolean aprovado = creditoGateway.consultarLimiteCredito(cpfCnpj, valorVenda);
        if (!aprovado) {
            throw new BusinessRuleException("Crédito rural reprovado para o produtor: saldo insuficiente para safra.");
        }
    }
}
