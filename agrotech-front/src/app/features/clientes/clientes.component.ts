import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { ClienteResponse } from '../../core/models/cliente.model';
import { ClienteService } from '../../core/services/cliente.service';
import { AuthService } from '../../core/services/auth.service';
import { extractApiErrorMessage } from '../../core/utils/api-error.util';
import { cpfCnpjValidator, telefoneValidator } from '../../core/utils/documentos.util';
import { MaskDirective } from '../../shared/directives/mask.directive';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-clientes',
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
      <app-page-header
        title="Clientes"
        subtitle="Cadastro de produtores rurais e empresas compradoras"
      >
        <p-button
          id="btn-novo-cliente"
          label="Novo Cliente"
          icon="pi pi-plus"
          severity="primary"
          (onClick)="abrirNovo()"
        />
      </app-page-header>

      <div class="surface-card">
        <p-table
          [value]="clientes()"
          [paginator]="true"
          [rows]="10"
          [loading]="loading()"
          styleClass="p-datatable-sm p-datatable-striped"
        >
          <ng-template #header>
            <tr>
              <th>Nome</th>
              <th>CPF / CNPJ</th>
              <th>Inscrição Estadual</th>
              <th>Telefone</th>
              <th>Endereço</th>
              <th style="width: 120px">Ações</th>
            </tr>
          </ng-template>
          <ng-template #body let-c>
            <tr>
              <td>{{ c.nome }}</td>
              <td>{{ c.cpfCnpj }}</td>
              <td>{{ c.inscricaoEstadual || '-' }}</td>
              <td>{{ c.telefone || '-' }}</td>
              <td>{{ c.endereco || '-' }}</td>
              <td>
                <div class="row-actions">
                  <p-button
                    [id]="'btn-editar-cliente-' + c.id"
                    icon="pi pi-pencil"
                    [text]="true"
                    severity="info"
                    (onClick)="abrirEdicao(c)"
                  />
                  @if (auth.isAdmin()) {
                    <p-button
                      [id]="'btn-excluir-cliente-' + c.id"
                      icon="pi pi-trash"
                      [text]="true"
                      severity="danger"
                      (onClick)="confirmarExclusao(c)"
                    />
                  }
                </div>
              </td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td colspan="6">Nenhum cliente cadastrado.</td>
            </tr>
          </ng-template>
        </p-table>
      </div>

      <p-dialog
        [(visible)]="dialogVisible"
        [header]="editandoId() ? 'Editar Cliente' : 'Novo Cliente'"
        [modal]="true"
        [style]="{ width: '640px', maxWidth: '95vw' }"
      >
        <form [formGroup]="form" (ngSubmit)="salvar()" class="dialog-form">
          <div class="form-grid">
            <div class="field-group">
              <label for="input-cliente-nome">Nome / Razão Social *</label>
              <input
                id="input-cliente-nome"
                type="text"
                pInputText
                formControlName="nome"
                maxlength="100"
                placeholder="Ex: Fazenda Santa Maria"
              />
              @if (form.controls.nome.invalid && form.controls.nome.touched) {
                <small class="field-error">Nome é obrigatório.</small>
              }
            </div>

            <div class="field-group">
              <label for="input-cliente-cpfcnpj">CPF / CNPJ *</label>
              <input
                id="input-cliente-cpfcnpj"
                type="text"
                pInputText
                appMask="cpfCnpj"
                formControlName="cpfCnpj"
                maxlength="18"
                placeholder="Ex: 000.000.000-00 ou 00.000.000/0001-00"
                inputmode="numeric"
              />
              @if (form.controls.cpfCnpj.touched && form.controls.cpfCnpj.errors; as e) {
                <small class="field-error">{{ e['required'] ? 'CPF/CNPJ é obrigatório.' : 'CPF/CNPJ inválido.' }}</small>
              }
            </div>

            <div class="field-group">
              <label for="input-cliente-ie">Inscrição Estadual</label>
              <input
                id="input-cliente-ie"
                type="text"
                pInputText
                formControlName="inscricaoEstadual"
                maxlength="50"
                placeholder="Ex: 10203040-5 ou ISENTO"
              />
            </div>

            <div class="field-group">
              <label for="input-cliente-telefone">Telefone</label>
              <input
                id="input-cliente-telefone"
                type="text"
                pInputText
                appMask="telefone"
                formControlName="telefone"
                maxlength="15"
                placeholder="Ex: (62) 99999-0000"
                inputmode="tel"
              />
              @if (form.controls.telefone.touched && form.controls.telefone.errors) {
                <small class="field-error">Telefone inválido. Use DDD + número.</small>
              }
            </div>

            <div class="field-group" style="grid-column: 1 / -1;">
              <label for="input-cliente-endereco">Endereço</label>
              <input
                id="input-cliente-endereco"
                type="text"
                pInputText
                formControlName="endereco"
                maxlength="200"
                placeholder="Ex: Rodovia GO-060, Km 12"
              />
            </div>
          </div>

          <div class="actions-bar">
            <p-button
              id="btn-cancelar-cliente"
              type="button"
              label="Cancelar"
              severity="secondary"
              [text]="true"
              (onClick)="dialogVisible = false"
            />
            <p-button
              id="btn-salvar-cliente"
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
export class ClientesComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly messages = inject(MessageService);
  private readonly confirmation = inject(ConfirmationService);

  protected readonly clientes = signal<ClienteResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected dialogVisible = false;

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required]],
    cpfCnpj: ['', [Validators.required, cpfCnpjValidator]],
    inscricaoEstadual: [''],
    telefone: ['', [telefoneValidator]],
    endereco: ['']
  });

  ngOnInit(): void {
    this.carregarClientes();
  }

  carregarClientes(): void {
    this.loading.set(true);
    this.clienteService.listar().subscribe({
      next: (page) => {
        this.clientes.set(page.content);
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
    this.form.reset({ nome: '', cpfCnpj: '', inscricaoEstadual: '', telefone: '', endereco: '' });
    this.dialogVisible = true;
  }

  abrirEdicao(c: ClienteResponse): void {
    this.editandoId.set(c.id);
    this.form.reset({
      nome: c.nome,
      cpfCnpj: c.cpfCnpj,
      inscricaoEstadual: c.inscricaoEstadual ?? '',
      telefone: c.telefone ?? '',
      endereco: c.endereco ?? ''
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
      nome: raw.nome.trim(),
      cpfCnpj: raw.cpfCnpj.trim(),
      inscricaoEstadual: raw.inscricaoEstadual.trim() || null,
      telefone: raw.telefone.trim() || null,
      endereco: raw.endereco.trim() || null
    };

    this.saving.set(true);
    const id = this.editandoId();
    const request$ = id
      ? this.clienteService.atualizar(id, payload)
      : this.clienteService.criar(payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogVisible = false;
        this.messages.add({
          severity: 'success',
          summary: 'Sucesso',
          detail: id ? 'Cliente atualizado com sucesso.' : 'Cliente cadastrado com sucesso.'
        });
        this.carregarClientes();
      },
      error: (err) => {
        this.saving.set(false);
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
      }
    });
  }

  confirmarExclusao(c: ClienteResponse): void {
    this.confirmation.confirm({
      header: 'Confirmar Exclusão',
      message: `Deseja excluir o cliente "${c.nome}"?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.clienteService.excluir(c.id).subscribe({
          next: () => {
            this.messages.add({
              severity: 'success',
              summary: 'Excluído',
              detail: 'Cliente removido com sucesso.'
            });
            this.carregarClientes();
          },
          error: (err) =>
            this.messages.add({
              severity: 'error',
              summary: 'Erro ao excluir',
              detail: extractApiErrorMessage(err)
            })
        });
      }
    });
  }
}

