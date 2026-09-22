package com.example.agrotech_api.externo;

import java.math.BigDecimal;

public interface AnaliseCreditoGateway {

    boolean consultarLimiteCredito(String cpfCnpj, BigDecimal valorVenda);

}
