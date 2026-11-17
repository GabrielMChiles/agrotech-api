package com.example.agrotech_api.externo;

import com.example.agrotech_api.exception.BusinessRuleException;
import com.example.agrotech_api.model.Venda;
import org.springframework.stereotype.Service;

@Service
public class SefazFiscalService {

    private final SefazFiscalGateway sefazGateway;

    public SefazFiscalService(SefazFiscalGateway sefazGateway) {
        this.sefazGateway = sefazGateway;
    }

    public String emitirNotaFiscal(Venda venda) {
        try {
            return sefazGateway.autorizarNotaFiscal(venda);
        } catch (Exception e) {
            throw new BusinessRuleException("Falha na comunicação com a SEFAZ: " + e.getMessage());
        }
    }
}
