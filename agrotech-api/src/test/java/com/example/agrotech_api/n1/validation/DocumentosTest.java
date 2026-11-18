package com.example.agrotech_api.n1.validation;

import com.example.agrotech_api.validation.Documentos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentosTest {

    @Test
    void cpf() {
        assertTrue(Documentos.cpfValido("529.982.247-25"));
        assertTrue(Documentos.cpfValido("52998224725"));
        assertFalse(Documentos.cpfValido("529.982.247-24"));
        assertFalse(Documentos.cpfValido("111.111.111-11"));
        assertFalse(Documentos.cpfValido("123"));
    }

    @Test
    void cnpj() {
        assertTrue(Documentos.cnpjValido("11.222.333/0001-81"));
        assertFalse(Documentos.cnpjValido("11.222.333/0001-82"));
        assertFalse(Documentos.cnpjValido("00.000.000/0000-00"));
    }

    @Test
    void telefone() {
        assertTrue(Documentos.telefoneValido("(62) 99999-0000"));
        assertTrue(Documentos.telefoneValido("6233331010"));
        assertFalse(Documentos.telefoneValido("(62) 89999-0000"));
        assertFalse(Documentos.telefoneValido("(02) 99999-0000"));
        assertFalse(Documentos.telefoneValido("99999"));
    }

    @Test
    void formatacao() {
        assertEquals("529.982.247-25", Documentos.formatarCpfOuCnpj("52998224725"));
        assertEquals("11.222.333/0001-81", Documentos.formatarCpfOuCnpj("11222333000181"));
        assertEquals("(62) 99999-0000", Documentos.formatarTelefone("62999990000"));
        assertEquals("(62) 3333-1010", Documentos.formatarTelefone("6233331010"));
    }
}
