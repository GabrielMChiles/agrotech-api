import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { RoleUsuario } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
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
        <h2 class="section-title">{{ editando() ? 'Editar Usuário' : 'Novo Usuário' }}</h2>
        <form [formGroup]="form" (ngSubmit)="salvar()">
          <div class="form-grid">
            <div class="field-group">
              <label for="input-usuario-username">Login (username)</label>
              <input
                id="input-usuario-username"
                type="text"
                pInputText
                formControlName="username"
                maxlength="60"
                placeholder="Ex: joao.vendas"
              />
              @if (form.controls.username.invalid && form.controls.username.touched) {
                <small class="field-error">Login obrigatório: 3+ caracteres, apenas letras, números, ponto, hífen e sublinhado.</small>
              }
            </div>

            <div class="field-group">
              <label for="input-usuario-senha">{{ editando() ? 'Nova senha (opcional)' : 'Senha' }}</label>
              <p-password
                inputId="input-usuario-senha"
                formControlName="senha"
                [maxlength]="72"
                [feedback]="false"
                [toggleMask]="true"
                [placeholder]="editando() ? 'Deixe em branco para manter' : 'Mínimo 4 caracteres'"
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
              @if (editando(); as u) {
                <div id="usuario-funcionario-vinculado" class="readonly-field">
                  {{ u.funcionarioNome }} ({{ u.funcionarioMatricula }})
                </div>
              } @else {
              <p-select
                inputId="select-usuario-funcionario"
                formControlName="funcionarioId"
                [options]="funcionariosDisponiveis()"
                optionLabel="nome"
                optionValue="id"
                placeholder="Selecione o funcionário"
                emptyMessage="Nenhum funcionário disponível. Cadastre um em Funcionários."
              />
              @if (form.controls.funcionarioId.invalid && form.controls.funcionarioId.touched) {
                <small class="field-error">Selecione um funcionário.</small>
              }
              }
            </div>
          </div>

          <div class="actions-bar">
            <p-button
              id="btn-salvar-usuario"
              type="submit"
              [label]="editando() ? 'Salvar Alterações' : 'Cadastrar Usuário'"
              icon="pi pi-check"
              severity="success"
              [loading]="saving()"
            />
            @if (editando()) {
              <p-button
                id="btn-cancelar-edicao-usuario"
                type="button"
                label="Cancelar edição"
                severity="secondary"
                [text]="true"
                (onClick)="cancelarEdicao()"
              />
            }
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
              <th style="width: 80px">Ações</th>
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
              <td>
                <p-button
                  [id]="'btn-editar-usuario-' + usuario.id"
                  icon="pi pi-pencil"
                  [text]="true"
                  severity="info"
                  (onClick)="editar(usuario)"
                />
              </td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td colspan="6">Nenhum usuário cadastrado.</td>
            </tr>
          </ng-template>
        </p-table>
      </div>
    </div>
  `,
  styles: `
    .readonly-field {
      padding: 0.65rem 0.75rem;
      background: var(--bg-page);
      border: 1px solid var(--border-subtle);
      border-radius: 6px;
      font-size: 0.925rem;
      color: var(--text-main);
    }

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
  private readonly auth = inject(AuthService);

  protected readonly usuarios = signal<UsuarioResponse[]>([]);
  protected readonly funcionarios = signal<FuncionarioResponse[]>([]);
  protected readonly funcionariosDisponiveis = computed(() => {
    const comAcesso = new Set(this.usuarios().map((u) => u.funcionarioId));
    return this.funcionarios().filter((f) => !comAcesso.has(f.id));
  });
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  /** Usuário em edição (null = modo cadastro). */
  protected readonly editando = signal<UsuarioResponse | null>(null);

  protected readonly roles: { label: string; value: RoleUsuario }[] = [
    { label: 'Administrador (ADMIN)', value: 'ADMIN' },
    { label: 'Funcionário (FUNCIONARIO)', value: 'FUNCIONARIO' }
  ];

  private readonly usernameValidators = [
    Validators.required,
    Validators.minLength(3),
    Validators.pattern(/^\s*[A-Za-z0-9._-]+\s*$/)
  ];

  protected readonly form = this.fb.group({
    username: ['', this.usernameValidators],
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

  editar(u: UsuarioResponse): void {
    this.editando.set(u);
    // Na edição a senha é opcional e o funcionário vinculado não muda
    this.form.controls.senha.setValidators([Validators.minLength(4)]);
    this.form.controls.funcionarioId.disable();
    this.form.reset({ username: u.username, senha: '', role: u.role, funcionarioId: u.funcionarioId });
    this.form.controls.senha.updateValueAndValidity();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelarEdicao(): void {
    this.editando.set(null);
    this.form.controls.senha.setValidators([Validators.required, Validators.minLength(4)]);
    this.form.controls.funcionarioId.enable();
    this.form.reset({ username: '', senha: '', role: 'FUNCIONARIO', funcionarioId: 0 });
    this.form.controls.senha.updateValueAndValidity();
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const emEdicao = this.editando();

    if (!emEdicao) {
      this.saving.set(true);
      this.usuarioService.criar({ ...raw, username: raw.username.trim() }).subscribe({
        next: () => this.concluir('Usuário cadastrado com sucesso.'),
        error: (err) => this.falhar(err)
      });
      return;
    }

    const username = raw.username.trim();
    const senha = raw.senha || null;
    const alterouAcesso = !!senha || username !== emEdicao.username || raw.role !== emEdicao.role;
    const ehOProprioUsuario = emEdicao.funcionarioId === this.auth.currentUser()?.funcionarioId;

    this.saving.set(true);
    this.usuarioService.atualizar(emEdicao.id, { username, senha, role: raw.role }).subscribe({
      next: () => {
        if (ehOProprioUsuario && alterouAcesso) {
          // O servidor encerrou as sessões deste usuário: é preciso entrar de novo
          this.saving.set(false);
          this.messages.add({
            severity: 'info',
            summary: 'Acesso alterado',
            detail: 'Seus dados de acesso mudaram. Entre novamente.'
          });
          this.auth.logout();
          return;
        }
        this.concluir(
          alterouAcesso
            ? 'Usuário atualizado. As sessões abertas dele foram encerradas.'
            : 'Usuário atualizado com sucesso.'
        );
      },
      error: (err) => this.falhar(err)
    });
  }

  private concluir(detail: string): void {
    this.saving.set(false);
    this.messages.add({ severity: 'success', summary: 'Sucesso', detail });
    this.cancelarEdicao();
    this.carregarDados();
  }

  private falhar(err: unknown): void {
    this.saving.set(false);
    this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
  }
}
