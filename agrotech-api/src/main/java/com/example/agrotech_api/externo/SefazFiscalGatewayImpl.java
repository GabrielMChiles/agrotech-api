package com.example.agrotech_api.externo;

import com.example.agrotech_api.model.Venda;
import org.springframework.stereotype.Component;

@Component
public class SefazFiscalGatewayImpl implements SefazFiscalGateway {

    @Override
    public String autorizarNotaFiscal(Venda venda) {
        return "NFE-PRODUCAO-AUTORIZADA-" + (venda != null ? venda.getId() : "0");
    }
}

