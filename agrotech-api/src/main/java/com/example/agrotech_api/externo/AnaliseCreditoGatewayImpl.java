package com.example.agrotech_api.externo;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class AnaliseCreditoGatewayImpl implements AnaliseCreditoGateway {

    @Override
    public boolean consultarLimiteCredito(String cpfCnpj, BigDecimal valorVenda) {
        return true;
    }
}

