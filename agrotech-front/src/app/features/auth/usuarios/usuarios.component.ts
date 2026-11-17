import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { RoleUsuario } from '../../../core/models/auth.model';
import { FuncionarioResponse, UsuarioResponse } from '../../../core/models/usuario.model';
import { UsuarioService } from '../../../core/services/usuario.service';
import { extractApiErrorMessage } from '../../../core/utils/api-error.util';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    PageHeaderComponent,
    InputTextModule,
    PasswordModule,
    SelectModule,
    ButtonModule,
    TableModule,
    TagModule
  ],
  template: `
    <div class="page-container">
      <app-page-header
        title="Gestão de Usuários"
        subtitle="Crie acessos ao sistema vinculados aos funcionários cadastrados"
      />

      <div class="surface-card">
        <h2 class="section-title">Novo Usuário</h2>
        <form [formGroup]="form" (ngSubmit)="salvar()">
          <div class="form-grid">
            <div class="field-group">
              <label for="input-usuario-username">Login (username)</label>
              <input
                id="input-usuario-username"
                type="text"
                pInputText
                formControlName="username"
                placeholder="Ex: joao.vendas"
              />
              @if (form.controls.username.invalid && form.controls.username.touched) {
                <small class="field-error">O login é obrigatório.</small>
              }
            </div>

            <div class="field-group">
              <label for="input-usuario-senha">Senha</label>
              <p-password
                inputId="input-usuario-senha"
                formControlName="senha"
                [feedback]="false"
                [toggleMask]="true"
                placeholder="Mínimo 4 caracteres"
              />
              @if (form.controls.senha.invalid && form.controls.senha.touched) {
                <small class="field-error">A senha deve ter ao menos 4 caracteres.</small>
              }
            </div>

            <div class="field-group">
              <label for="select-usuario-role">Perfil</label>
              <p-select
                inputId="select-usuario-role"
                formControlName="role"
                [options]="roles"
                optionLabel="label"
                optionValue="value"
                placeholder="Selecione o perfil"
              />
            </div>

            <div class="field-group">
              <label for="select-usuario-funcionario">Funcionário</label>
              <p-select
                inputId="select-usuario-funcionario"
                formControlName="funcionarioId"
                [options]="funcionarios()"
                optionLabel="nome"
                optionValue="id"
                placeholder="Selecione o funcionário"
              />
              @if (form.controls.funcionarioId.invalid && form.controls.funcionarioId.touched) {
                <small class="field-error">Selecione um funcionário.</small>
              }
            </div>
          </div>

          <div class="actions-bar">
            <p-button
              id="btn-salvar-usuario"
              type="submit"
              label="Cadastrar Usuário"
              icon="pi pi-check"
              severity="success"
              [loading]="saving()"
            />
          </div>
        </form>
      </div>

      <div class="surface-card mt-lg">
        <p-table
          [value]="usuarios()"
          [paginator]="true"
          [rows]="10"
          [loading]="loading()"
          styleClass="p-datatable-sm p-datatable-striped"
        >
          <ng-template #header>
            <tr>
              <th>ID</th>
              <th>Usuário</th>
              <th>Perfil</th>
              <th>Funcionário</th>
              <th>Matrícula</th>
            </tr>
          </ng-template>
          <ng-template #body let-usuario>
            <tr>
              <td>{{ usuario.id }}</td>
              <td>{{ usuario.username }}</td>
              <td>
                <p-tag
                  [value]="usuario.role"
                  [severity]="usuario.role === 'ADMIN' ? 'success' : 'info'"
                />
              </td>
              <td>{{ usuario.funcionarioNome }}</td>
              <td>{{ usuario.funcionarioMatricula }}</td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td colspan="5">Nenhum usuário cadastrado.</td>
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
  `
})
export class UsuariosComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly messages = inject(MessageService);

  protected readonly usuarios = signal<UsuarioResponse[]>([]);
  protected readonly funcionarios = signal<FuncionarioResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);

  protected readonly roles: { label: string; value: RoleUsuario }[] = [
    { label: 'Administrador (ADMIN)', value: 'ADMIN' },
    { label: 'Funcionário (FUNCIONARIO)', value: 'FUNCIONARIO' }
  ];

  protected readonly form = this.fb.group({
    username: ['', [Validators.required]],
    senha: ['', [Validators.required, Validators.minLength(4)]],
    role: ['FUNCIONARIO' as RoleUsuario, [Validators.required]],
    funcionarioId: [0, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    this.loading.set(true);
    this.usuarioService.listarTodos().subscribe({
      next: (lista) => {
        this.usuarios.set(lista);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
      }
    });

    this.usuarioService.listarFuncionarios().subscribe({
      next: (page) => this.funcionarios.set(page.content),
      error: (err) =>
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) })
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.usuarioService.criar(this.form.getRawValue()).subscribe({
      next: () => {
        this.saving.set(false);
        this.messages.add({
          severity: 'success',
          summary: 'Sucesso',
          detail: 'Usuário cadastrado com sucesso.'
        });
        this.form.reset({ username: '', senha: '', role: 'FUNCIONARIO', funcionarioId: 0 });
        this.carregarDados();
      },
      error: (err) => {
        this.saving.set(false);
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
      }
    });
  }
}

