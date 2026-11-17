import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { AuthService } from '../../../core/services/auth.service';
import { extractApiErrorMessage } from '../../../core/utils/api-error.util';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, InputTextModule, PasswordModule, ButtonModule],
  template: `
    <div class="login-wrapper">
      <div class="surface-card login-card">
        <div class="login-brand">
          <i class="pi pi-leaf brand-icon"></i>
          <div>
            <h1>AgroTech System</h1>
            <p class="text-muted">Acesse com suas credenciais</p>
          </div>
        </div>

        <form [formGroup]="form" (ngSubmit)="entrar()" class="login-form">
          <div class="field-group">
            <label for="input-login-username">Usuário</label>
            <input
              id="input-login-username"
              type="text"
              pInputText
              formControlName="username"
              placeholder="Ex: admin"
              autocomplete="username"
            />
            @if (form.controls.username.invalid && form.controls.username.touched) {
              <small class="field-error">Informe o usuário.</small>
            }
          </div>

          <div class="field-group">
            <label for="input-login-senha">Senha</label>
            <p-password
              inputId="input-login-senha"
              formControlName="senha"
              [feedback]="false"
              [toggleMask]="true"
              placeholder="Digite sua senha"
              autocomplete="current-password"
            />
            @if (form.controls.senha.invalid && form.controls.senha.touched) {
              <small class="field-error">Informe a senha.</small>
            }
          </div>

          <p-button
            id="btn-login-entrar"
            type="submit"
            label="Entrar"
            icon="pi pi-sign-in"
            severity="success"
            [loading]="loading()"
            [style]="{ width: '100%' }"
          />
        </form>

        <div class="demo-hint text-muted">
          <small>Acesso padrão: <strong>admin</strong> / <strong>admin123</strong> (ADMIN) ou <strong>ana</strong> / <strong>ana123</strong> (FUNCIONARIO)</small>
        </div>
      </div>
    </div>
  `,
  styles: `
    .login-wrapper {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1.5rem;
    }

    .login-card {
      width: 100%;
      max-width: 420px;
    }

    .login-brand {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 1.5rem;
    }

    .brand-icon {
      font-size: 2rem;
      color: var(--primary-color);
    }

    h1 {
      margin: 0;
      font-size: 1.35rem;
    }

    p {
      margin: 0.15rem 0 0;
      font-size: 0.875rem;
    }

    .login-form {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .demo-hint {
      margin-top: 1.25rem;
      padding-top: 1rem;
      border-top: 1px solid var(--border-subtle);
      text-align: center;
    }
  `
})
export class LoginComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly messages = inject(MessageService);

  protected readonly loading = signal(false);

  protected readonly form = this.fb.group({
    username: ['', [Validators.required]],
    senha: ['', [Validators.required]]
  });

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (session) => {
        this.loading.set(false);
        this.messages.add({
          severity: 'success',
          summary: 'Bem-vindo',
          detail: `Sessão iniciada como ${session.funcionarioNome}.`
        });
        this.router.navigate(['/vendas']);
      },
      error: (err) => {
        this.loading.set(false);
        this.messages.add({
          severity: 'error',
          summary: 'Falha no login',
          detail: extractApiErrorMessage(err)
        });
      }
    });
  }
}

