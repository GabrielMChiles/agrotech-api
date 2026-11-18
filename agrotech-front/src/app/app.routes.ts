
import { Routes } from '@angular/router';

import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent
      )
  },
  {
    path: 'vendas',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/vendas/historico-vendas/historico-vendas.component').then(
        (m) => m.HistoricoVendasComponent
      )
  },
  {
    path: 'vendas/nova',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/vendas/nova-venda/nova-venda.component').then(
        (m) => m.NovaVendaComponent
      )
  },
  {
    path: 'clientes',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/clientes/clientes.component').then(
        (m) => m.ClientesComponent
      )
  },
  {
    path: 'produtos',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/produtos/produtos.component').then(
        (m) => m.ProdutosComponent
      )
  },
  {
    path: 'fornecedores',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/fornecedores/fornecedores.component').then(
        (m) => m.FornecedoresComponent
      )
  },
  {
    path: 'funcionarios',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./features/funcionarios/funcionarios.component').then(
        (m) => m.FuncionariosComponent
      )
  },
  {
    path: 'usuarios',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./features/auth/usuarios/usuarios.component').then(
        (m) => m.UsuariosComponent
      )
  },
  {
    path: '',
    redirectTo: 'vendas',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'vendas'
  }
];
