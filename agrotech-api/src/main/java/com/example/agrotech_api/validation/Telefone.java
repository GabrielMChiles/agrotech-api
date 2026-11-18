package com.example.agrotech_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Telefone.Validador.class)
public @interface Telefone {
    String message() default "Telefone inválido. Use DDD + número, ex: (62) 99999-0000";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /** Campos vazios são aceitos; combine com @NotBlank quando o campo for obrigatório. */
    class Validador implements ConstraintValidator<Telefone, String> {
        @Override
        public boolean isValid(String valor, ConstraintValidatorContext ctx) {
            return valor == null || valor.isBlank() || Documentos.telefoneValido(valor);
        }
    }
}
