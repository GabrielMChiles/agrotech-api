import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { FornecedorResponse } from '../../core/models/produto.model';
import { AuthService } from '../../core/services/auth.service';
import { FornecedorService } from '../../core/services/fornecedor.service';
import { extractApiErrorMessage } from '../../core/utils/api-error.util';
import { cnpjValidator, telefoneValidator } from '../../core/utils/documentos.util';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { MaskDirective } from '../../shared/directives/mask.directive';

@Component({
  selector: 'app-fornecedores',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    PageHeaderComponent,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    MaskDirective
  ],
  template: `
    <div class="page-container">
      <app-page-header title="Fornecedores" subtitle="Cadastro de fornecedores dos produtos">
        <p-button
          id="btn-novo-fornecedor"
          label="Novo Fornecedor"
          icon="pi pi-plus"
          severity="primary"
          (onClick)="abrirNovo()"
        />
      </app-page-header>

      <div class="surface-card">
        <p-table
          [value]="fornecedores()"
          [paginator]="true"
          [rows]="10"
          [loading]="loading()"
          styleClass="p-datatable-sm p-datatable-striped"
        >
          <ng-template #header>
            <tr>
              <th>Razão Social</th>
              <th>CNPJ</th>
              <th>Telefone</th>
              <th style="width: 120px">Ações</th>
            </tr>
          </ng-template>
          <ng-template #body let-f>
            <tr>
              <td>{{ f.razaoSocial }}</td>
              <td>{{ f.cnpj }}</td>
              <td>{{ f.telefoneContato || '-' }}</td>
              <td>
                <div class="row-actions">
                  <p-button
                    [id]="'btn-editar-fornecedor-' + f.id"
                    icon="pi pi-pencil"
                    [text]="true"
                    severity="info"
                    (onClick)="abrirEdicao(f)"
                  />
                  @if (auth.isAdmin()) {
                    <p-button
                      [id]="'btn-excluir-fornecedor-' + f.id"
                      icon="pi pi-trash"
                      [text]="true"
                      severity="danger"
                      (onClick)="confirmarExclusao(f)"
                    />
                  }
                </div>
              </td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td colspan="4">Nenhum fornecedor cadastrado.</td>
            </tr>
          </ng-template>
        </p-table>
      </div>

      <p-dialog
        [(visible)]="dialogVisible"
        [header]="editandoId() ? 'Editar Fornecedor' : 'Novo Fornecedor'"
        [modal]="true"
        [style]="{ width: '560px', maxWidth: '95vw' }"
      >
        <form [formGroup]="form" (ngSubmit)="salvar()" class="dialog-form">
          <div class="form-grid">
            <div class="field-group" style="grid-column: 1 / -1;">
              <label for="input-fornecedor-razao">Razão Social *</label>
              <input
                id="input-fornecedor-razao"
                type="text"
                pInputText
                formControlName="razaoSocial"
                maxlength="100"
                placeholder="Ex: AgroSementes Brasil Ltda"
              />
              @if (form.controls.razaoSocial.invalid && form.controls.razaoSocial.touched) {
                <small class="field-error">Razão social é obrigatória.</small>
              }
            </div>

            <div class="field-group">
              <label for="input-fornecedor-cnpj">CNPJ *</label>
              <input
                id="input-fornecedor-cnpj"
                type="text"
                pInputText
                appMask="cnpj"
                formControlName="cnpj"
                maxlength="18"
                placeholder="00.000.000/0000-00"
                inputmode="numeric"
              />
              @if (form.controls.cnpj.touched && form.controls.cnpj.errors; as e) {
                <small class="field-error">{{ e['required'] ? 'CNPJ é obrigatório.' : 'CNPJ inválido.' }}</small>
              }
            </div>

            <div class="field-group">
              <label for="input-fornecedor-telefone">Telefone</label>
              <input
                id="input-fornecedor-telefone"
                type="text"
                pInputText
                appMask="telefone"
                formControlName="telefoneContato"
                maxlength="15"
                placeholder="(62) 99999-0000"
                inputmode="tel"
              />
              @if (form.controls.telefoneContato.touched && form.controls.telefoneContato.errors) {
                <small class="field-error">Telefone inválido. Use DDD + número.</small>
              }
            </div>
          </div>

          <div class="actions-bar">
            <p-button
              id="btn-cancelar-fornecedor"
              type="button"
              label="Cancelar"
              severity="secondary"
              [text]="true"
              (onClick)="dialogVisible = false"
            />
            <p-button
              id="btn-salvar-fornecedor"
              type="submit"
              label="Salvar"
              icon="pi pi-check"
              severity="success"
              [loading]="saving()"
            />
          </div>
        </form>
      </p-dialog>
    </div>
  `,
  styles: `
    .row-actions {
      display: flex;
      gap: 0.25rem;
    }

    .dialog-form {
      padding-top: 0.5rem;
    }
  `
})
export class FornecedoresComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly fornecedorService = inject(FornecedorService);
  private readonly messages = inject(MessageService);
  private readonly confirmation = inject(ConfirmationService);
  protected readonly auth = inject(AuthService);

  protected readonly fornecedores = signal<FornecedorResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected dialogVisible = false;

  protected readonly form = this.fb.group({
    razaoSocial: ['', [Validators.required]],
    cnpj: ['', [Validators.required, cnpjValidator]],
    telefoneContato: ['', [telefoneValidator]]
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.fornecedorService.listar().subscribe({
      next: (page) => {
        this.fornecedores.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
      }
    });
  }

  abrirNovo(): void {
    this.editandoId.set(null);
    this.form.reset({ razaoSocial: '', cnpj: '', telefoneContato: '' });
    this.dialogVisible = true;
  }

  abrirEdicao(f: FornecedorResponse): void {
    this.editandoId.set(f.id);
    this.form.reset({
      razaoSocial: f.razaoSocial,
      cnpj: f.cnpj,
      telefoneContato: f.telefoneContato ?? ''
    });
    this.dialogVisible = true;
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload = {
      razaoSocial: raw.razaoSocial.trim(),
      cnpj: raw.cnpj.trim(),
      telefoneContato: raw.telefoneContato.trim() || null
    };

    this.saving.set(true);
    const id = this.editandoId();
    const request$ = id
      ? this.fornecedorService.atualizar(id, payload)
      : this.fornecedorService.criar(payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogVisible = false;
        this.messages.add({
          severity: 'success',
          summary: 'Sucesso',
          detail: id ? 'Fornecedor atualizado com sucesso.' : 'Fornecedor cadastrado com sucesso.'
        });
        this.carregar();
      },
      error: (err) => {
        this.saving.set(false);
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
      }
    });
  }

  confirmarExclusao(f: FornecedorResponse): void {
    this.confirmation.confirm({
      header: 'Confirmar Exclusão',
      message: `Deseja excluir o fornecedor "${f.razaoSocial}"?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.fornecedorService.excluir(f.id).subscribe({
          next: () => {
            this.messages.add({ severity: 'success', summary: 'Excluído', detail: 'Fornecedor removido com sucesso.' });
            this.carregar();
          },
          error: (err) =>
            this.messages.add({ severity: 'error', summary: 'Erro ao excluir', detail: extractApiErrorMessage(err) })
        });
      }
    });
  }
}
