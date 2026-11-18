import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { FuncionarioResponse } from '../../core/models/usuario.model';
import { UsuarioService } from '../../core/services/usuario.service';
import { extractApiErrorMessage } from '../../core/utils/api-error.util';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-funcionarios',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    PageHeaderComponent,
    InputTextModule,
    ButtonModule,
    TableModule
  ],
  template: `
    <div class="page-container">
      <app-page-header
        title="Gestão de Funcionários"
        subtitle="Cadastre o funcionário primeiro; depois crie o acesso dele em Usuários"
      />

      <div class="surface-card">
        <h2 class="section-title">{{ editandoId() ? 'Editar Funcionário' : 'Novo Funcionário' }}</h2>
        <form [formGroup]="form" (ngSubmit)="salvar()">
          <div class="form-grid">
            <div class="field-group">
              <label for="input-funcionario-nome">Nome</label>
              <input
                id="input-funcionario-nome"
                type="text"
                pInputText
                formControlName="nome"
                maxlength="100"
                placeholder="Ex: Maria Santos"
              />
              @if (form.controls.nome.invalid && form.controls.nome.touched) {
                <small class="field-error">O nome é obrigatório.</small>
              }
            </div>

            <div class="field-group">
              <label for="input-funcionario-matricula">Matrícula</label>
              <input
                id="input-funcionario-matricula"
                type="text"
                pInputText
                formControlName="matricula"
                maxlength="20"
                placeholder="Ex: M04"
              />
              @if (form.controls.matricula.invalid && form.controls.matricula.touched) {
                <small class="field-error">A matrícula é obrigatória.</small>
              }
            </div>

            <div class="field-group">
              <label for="input-funcionario-cargo">Cargo</label>
              <input
                id="input-funcionario-cargo"
                type="text"
                pInputText
                formControlName="cargo"
                maxlength="50"
                placeholder="Ex: Vendedor"
              />
              @if (form.controls.cargo.invalid && form.controls.cargo.touched) {
                <small class="field-error">O cargo é obrigatório.</small>
              }
            </div>
          </div>

          <div class="actions-bar">
            <p-button
              id="btn-salvar-funcionario"
              type="submit"
              [label]="editandoId() ? 'Salvar Alterações' : 'Cadastrar Funcionário'"
              icon="pi pi-check"
              severity="success"
              [loading]="saving()"
            />
            @if (editandoId()) {
              <p-button
                id="btn-cancelar-edicao-funcionario"
                type="button"
                label="Cancelar edição"
                severity="secondary"
                [text]="true"
                (onClick)="cancelarEdicao()"
              />
            }
            <a routerLink="/usuarios" class="link-usuarios">Ir para Usuários</a>
          </div>
        </form>
      </div>

      <div class="surface-card mt-lg">
        <p-table
          [value]="funcionarios()"
          [paginator]="true"
          [rows]="10"
          [loading]="loading()"
          styleClass="p-datatable-sm p-datatable-striped"
        >
          <ng-template #header>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Matrícula</th>
              <th>Cargo</th>
              <th style="width: 80px">Ações</th>
            </tr>
          </ng-template>
          <ng-template #body let-funcionario>
            <tr>
              <td>{{ funcionario.id }}</td>
              <td>{{ funcionario.nome }}</td>
              <td>{{ funcionario.matricula }}</td>
              <td>{{ funcionario.cargo }}</td>
              <td>
                <p-button
                  [id]="'btn-editar-funcionario-' + funcionario.id"
                  icon="pi pi-pencil"
                  [text]="true"
                  severity="info"
                  (onClick)="editar(funcionario)"
                />
              </td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td colspan="5">Nenhum funcionário cadastrado.</td>
            </tr>
          </ng-template>
        </p-table>
      </div>
    </div>
  `,
  styles: `
    .section-title {
      margin: 0 0 1rem;
      font-size: 1.05rem;
      font-weight: 600;
    }

    .link-usuarios {
      margin-left: 1rem;
      font-size: 0.9rem;
      color: var(--primary-hover);
    }
  `
})
export class FuncionariosComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly messages = inject(MessageService);

  protected readonly funcionarios = signal<FuncionarioResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly editandoId = signal<number | null>(null);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required]],
    matricula: ['', [Validators.required]],
    cargo: ['', [Validators.required]]
  });

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    this.loading.set(true);
    this.usuarioService.listarFuncionarios().subscribe({
      next: (page) => {
        this.funcionarios.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
      }
    });
  }

  editar(f: FuncionarioResponse): void {
    this.editandoId.set(f.id);
    this.form.reset({ nome: f.nome, matricula: f.matricula, cargo: f.cargo });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelarEdicao(): void {
    this.editandoId.set(null);
    this.form.reset({ nome: '', matricula: '', cargo: '' });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload = { nome: raw.nome.trim(), matricula: raw.matricula.trim(), cargo: raw.cargo.trim() };
    const id = this.editandoId();
    const request$ = id
      ? this.usuarioService.atualizarFuncionario(id, payload)
      : this.usuarioService.criarFuncionario(payload);

    this.saving.set(true);
    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.messages.add({
          severity: 'success',
          summary: 'Sucesso',
          detail: id ? 'Funcionário atualizado com sucesso.' : 'Funcionário cadastrado com sucesso.'
        });
        this.cancelarEdicao();
        this.carregarDados();
      },
      error: (err) => {
        this.saving.set(false);
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
      }
    });
  }
}
