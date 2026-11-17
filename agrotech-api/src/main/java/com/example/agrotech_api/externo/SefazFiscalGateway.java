package com.example.agrotech_api.externo;

import com.example.agrotech_api.model.Venda;

public interface SefazFiscalGateway {

    String autorizarNotaFiscal(Venda venda);

}
