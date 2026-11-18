import { Directive, ElementRef, HostListener, inject, input } from '@angular/core';
import { NgControl } from '@angular/forms';
import { mascaraCnpj, mascaraCpfCnpj, mascaraTelefone } from '../../core/utils/documentos.util';

export type TipoMascara = 'cpfCnpj' | 'cnpj' | 'telefone';

@Directive({ selector: 'input[appMask]', standalone: true })
export class MaskDirective {
  readonly appMask = input.required<TipoMascara>();

  private readonly el = inject<ElementRef<HTMLInputElement>>(ElementRef);
  private readonly control = inject(NgControl, { optional: true });

  @HostListener('input')
  aoDigitar(): void {
    const tipo = this.appMask();
    const bruto = this.el.nativeElement.value;
    const formatado =
      tipo === 'telefone' ? mascaraTelefone(bruto) : tipo === 'cnpj' ? mascaraCnpj(bruto) : mascaraCpfCnpj(bruto);
    this.el.nativeElement.value = formatado;
    this.control?.control?.setValue(formatado, { emitEvent: false });
    this.control?.control?.updateValueAndValidity({ emitEvent: false });
  }
}
