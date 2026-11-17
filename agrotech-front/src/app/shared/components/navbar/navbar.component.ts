import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, ButtonModule, TagModule],
  template: `
    @if (auth.isAuthenticated()) {
      <header class="navbar">
        <div class="navbar-inner">
          <div class="navbar-left">
            <a routerLink="/vendas" class="brand">
              <i class="pi pi-leaf brand-icon"></i>
              <span>AgroTech</span>
            </a>

            <nav class="nav-links">
              <a
                id="nav-vendas"
                routerLink="/vendas"
                routerLinkActive="active"
                [routerLinkActiveOptions]="{ exact: true }"
              >
                Vendas
              </a>
              <a id="nav-nova-venda" routerLink="/vendas/nova" routerLinkActive="active">
                Nova Venda
              </a>
              <a id="nav-clientes" routerLink="/clientes" routerLinkActive="active">
                Clientes
              </a>
              <a id="nav-produtos" routerLink="/produtos" routerLinkActive="active">
                Produtos
              </a>
              @if (auth.isAdmin()) {
                <a id="nav-usuarios" routerLink="/usuarios" routerLinkActive="active">
                  Usuários
                </a>
              }
            </nav>
          </div>

          <div class="navbar-right">
            <span class="user-name">{{ auth.currentUser()?.funcionarioNome }}</span>
            <p-tag
              [value]="auth.currentUser()?.role ?? ''"
              [severity]="auth.isAdmin() ? 'success' : 'info'"
            />
            <p-button
              id="btn-logout"
              label="Sair"
              icon="pi pi-sign-out"
              severity="secondary"
              [text]="true"
              size="small"
              (onClick)="auth.logout()"
            />
          </div>
        </div>
      </header>
    }
  `,
  styles: `
    .navbar {
      background: var(--bg-surface);
      border-bottom: 1px solid var(--border-subtle);
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .navbar-inner {
      max-width: 1280px;
      margin: 0 auto;
      padding: 0.75rem 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .navbar-left {
      display: flex;
      align-items: center;
      gap: 2rem;
      flex-wrap: wrap;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      text-decoration: none;
      color: var(--text-main);
      font-weight: 700;
      font-size: 1.15rem;
    }

    .brand-icon {
      color: var(--primary-color);
      font-size: 1.25rem;
    }

    .nav-links {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      flex-wrap: wrap;
    }

    .nav-links a {
      text-decoration: none;
      color: var(--text-muted);
      font-size: 0.9rem;
      font-weight: 500;
      padding: 0.45rem 0.75rem;
      border-radius: 6px;
      transition: background-color 0.15s, color 0.15s;
    }

    .nav-links a:hover {
      background: var(--bg-page);
      color: var(--text-main);
    }

    .nav-links a.active {
      background: #dcfce7;
      color: var(--primary-hover);
      font-weight: 600;
    }

    .navbar-right {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }

    .user-name {
      font-size: 0.875rem;
      font-weight: 600;
      color: var(--text-main);
    }
  `
})
export class NavbarComponent {
  protected readonly auth = inject(AuthService);
}

