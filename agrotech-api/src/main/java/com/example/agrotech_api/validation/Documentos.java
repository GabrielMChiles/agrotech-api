package com.example.agrotech_api.validation;

/** Utilitário de validação e formatação de CPF, CNPJ e telefone brasileiros. */
public final class Documentos {

    private Documentos() {}

    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean cpfValido(String valor) {
        String d = somenteDigitos(valor);
        if (d.length() != 11 || todosIguais(d)) return false;
        return digitoCpf(d, 9, 10) == d.charAt(9) - '0' && digitoCpf(d, 10, 11) == d.charAt(10) - '0';
    }

    public static boolean cnpjValido(String valor) {
        String d = somenteDigitos(valor);
        if (d.length() != 14 || todosIguais(d)) return false;
        return digitoCnpj(d, 12) == d.charAt(12) - '0' && digitoCnpj(d, 13) == d.charAt(13) - '0';
    }

    public static boolean cpfCnpjValido(String valor) {
        String d = somenteDigitos(valor);
        return d.length() == 11 ? cpfValido(valor) : cnpjValido(valor);
    }

    /** Aceita fixo (DDD + 8 dígitos iniciando em 2-5) ou celular (DDD + 9 dígitos iniciando em 9). */
    public static boolean telefoneValido(String valor) {
        String d = somenteDigitos(valor);
        if (d.length() != 10 && d.length() != 11) return false;
        int ddd = Integer.parseInt(d.substring(0, 2));
        if (ddd < 11) return false;
        char primeiro = d.charAt(2);
        return d.length() == 10 ? (primeiro >= '2' && primeiro <= '5') : primeiro == '9';
    }

    public static String formatarCpfOuCnpj(String valor) {
        String d = somenteDigitos(valor);
        if (d.length() == 11) {
            return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
        }
        if (d.length() == 14) {
            return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8)
                    + "/" + d.substring(8, 12) + "-" + d.substring(12);
        }
        return valor;
    }

    public static String formatarTelefone(String valor) {
        if (valor == null || valor.isBlank()) return valor;
        String d = somenteDigitos(valor);
        if (d.length() == 11) return "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
        if (d.length() == 10) return "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
        return valor;
    }

    private static boolean todosIguais(String d) {
        return d.chars().distinct().count() == 1;
    }

    private static int digitoCpf(String d, int tamanho, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) soma += (d.charAt(i) - '0') * (pesoInicial - i);
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static int digitoCnpj(String d, int tamanho) {
        int soma = 0;
        int peso = tamanho - 7;
        for (int i = 0; i < tamanho; i++) {
            soma += (d.charAt(i) - '0') * peso--;
            if (peso < 2) peso = 9;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
