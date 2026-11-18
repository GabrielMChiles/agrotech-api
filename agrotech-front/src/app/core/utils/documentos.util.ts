import { AbstractControl, ValidationErrors } from '@angular/forms';

export const somenteDigitos = (v: string | null | undefined): string => (v ?? '').replace(/\D/g, '');

const todosIguais = (d: string): boolean => /^(\d)\1+$/.test(d);

function digitoCpf(d: string, tamanho: number): number {
  let soma = 0;
  for (let i = 0; i < tamanho; i++) soma += Number(d[i]) * (tamanho + 1 - i);
  const resto = (soma * 10) % 11;
  return resto === 10 ? 0 : resto;
}

function digitoCnpj(d: string, tamanho: number): number {
  let soma = 0;
  let peso = tamanho - 7;
  for (let i = 0; i < tamanho; i++) {
    soma += Number(d[i]) * peso--;
    if (peso < 2) peso = 9;
  }
  const resto = soma % 11;
  return resto < 2 ? 0 : 11 - resto;
}

export function cpfValido(v: string): boolean {
  const d = somenteDigitos(v);
  return d.length === 11 && !todosIguais(d) && digitoCpf(d, 9) === Number(d[9]) && digitoCpf(d, 10) === Number(d[10]);
}

export function cnpjValido(v: string): boolean {
  const d = somenteDigitos(v);
  return d.length === 14 && !todosIguais(d) && digitoCnpj(d, 12) === Number(d[12]) && digitoCnpj(d, 13) === Number(d[13]);
}

export function telefoneValido(v: string): boolean {
  const d = somenteDigitos(v);
  if (d.length !== 10 && d.length !== 11) return false;
  if (Number(d.slice(0, 2)) < 11) return false;
  return d.length === 10 ? /[2-5]/.test(d[2]) : d[2] === '9';
}

// Validators (campos vazios são aceitos: combine com Validators.required quando obrigatório)
export const cpfCnpjValidator = (c: AbstractControl): ValidationErrors | null => {
  const d = somenteDigitos(c.value);
  if (!d) return null;
  const ok = d.length === 11 ? cpfValido(d) : cnpjValido(d);
  return ok ? null : { cpfCnpj: true };
};

export const cnpjValidator = (c: AbstractControl): ValidationErrors | null =>
  !somenteDigitos(c.value) || cnpjValido(c.value) ? null : { cnpj: true };

export const telefoneValidator = (c: AbstractControl): ValidationErrors | null =>
  !somenteDigitos(c.value) || telefoneValido(c.value) ? null : { telefone: true };

// Máscaras
export function mascaraCpfCnpj(v: string): string {
  const d = somenteDigitos(v).slice(0, 14);
  if (d.length <= 11) {
    return d
      .replace(/^(\d{3})(\d)/, '$1.$2')
      .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
      .replace(/\.(\d{3})(\d)/, '.$1-$2');
  }
  return d
    .replace(/^(\d{2})(\d)/, '$1.$2')
    .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1/$2')
    .replace(/(\d{4})(\d)/, '$1-$2');
}

export function mascaraCnpj(v: string): string {
  const d = somenteDigitos(v).slice(0, 14);
  return d
    .replace(/^(\d{2})(\d)/, '$1.$2')
    .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1/$2')
    .replace(/(\d{4})(\d)/, '$1-$2');
}

export function mascaraTelefone(v: string): string {
  const d = somenteDigitos(v).slice(0, 11);
  if (d.length <= 2) return d ? `(${d}` : '';
  if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`;
  if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
  return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
}
