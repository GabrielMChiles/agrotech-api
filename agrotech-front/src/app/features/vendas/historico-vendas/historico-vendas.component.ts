import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { VendaResponse } from '../../../core/models/venda.model';
import { AuthService } from '../../../core/services/auth.service';
import { VendaService } from '../../../core/services/venda.service';
import { extractApiErrorMessage } from '../../../core/utils/api-error.util';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-historico-vendas',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink,
    PageHeaderComponent,
    TableModule,
    ButtonModule,
    TagModule,
    DialogModule
  ],
  template: `
    <div class="page-container">
      <app-page-header
        title="Histórico de Vendas"
        subtitle="Acompanhe vendas efetivadas e realize cancelamentos com estorno de estoque"
      >
        <p-button
          id="btn-ir-nova-venda"
          label="Nova Venda"
          icon="pi pi-plus"
          severity="primary"
          routerLink="/vendas/nova"
        />
      </app-page-header>

      <div class="surface-card">
        <p-table
          [value]="vendas()"
          [paginator]="true"
          [rows]="10"
          [loading]="loading()"
          styleClass="p-datatable-sm p-datatable-striped"
        >
          <ng-template #header>
            <tr>
              <th>#</th>
              <th>Data/Hora</th>
              <th>Cliente</th>
              <th>Vendedor</th>
              <th>Valor Total</th>
              <th>Status</th>
              <th style="width: 220px">Ações</th>
            </tr>
          </ng-template>
          <ng-template #body let-v>
            <tr>
              <td>{{ v.id }}</td>
              <td>{{ v.dataHoraVenda | date: 'dd/MM/yyyy HH:mm' }}</td>
              <td>{{ v.nomeCliente }}</td>
              <td>{{ v.nomeFuncionario }}</td>
              <td>{{ v.valorTotal | currency: 'BRL' }}</td>
              <td>
                <p-tag
                  [value]="v.status"
                  [severity]="v.status === 'CONCLUIDA' ? 'success' : 'danger'"
                />
              </td>
              <td>
                <div class="row-actions">
                  <p-button
                    [id]="'btn-detalhes-venda-' + v.id"
                    label="Itens"
                    icon="pi pi-eye"
                    [text]="true"
                    size="small"
                    severity="info"
                    (onClick)="verDetalhes(v)"
                  />
                  @if (auth.isAdmin()) {
                    <p-button
                      [id]="'btn-cancelar-venda-' + v.id"
                      label="Cancelar"
                      icon="pi pi-times-circle"
                      [text]="true"
                      size="small"
                      severity="danger"
                      [disabled]="v.status === 'CANCELADA'"
                      (onClick)="confirmarCancelamento(v)"
                    />
                  }
                </div>
              </td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td colspan="7">Nenhuma venda registrada até o momento.</td>
            </tr>
          </ng-template>
        </p-table>
      </div>

      <p-dialog
        [(visible)]="detalhesVisible"
        [header]="'Itens da Venda #' + (vendaSelecionada()?.id ?? '')"
        [modal]="true"
        [style]="{ width: '600px', maxWidth: '95vw' }"
      >
        @if (vendaSelecionada(); as venda) {
          <p class="text-muted" style="margin-top: 0">
            Cliente: <strong>{{ venda.nomeCliente }}</strong> | Vendedor:
            <strong>{{ venda.nomeFuncionario }}</strong>
          </p>

          <p-table [value]="venda.itens" styleClass="p-datatable-sm p-datatable-striped">
            <ng-template #header>
              <tr>
                <th>Produto</th>
                <th>Qtd</th>
                <th>Preço Unit.</th>
                <th>Subtotal</th>
              </tr>
            </ng-template>
            <ng-template #body let-item>
              <tr>
                <td>{{ item.nomeProduto }}</td>
                <td>{{ item.quantidade }}</td>
                <td>{{ item.precoUnitario | currency: 'BRL' }}</td>
                <td>{{ item.subtotal | currency: 'BRL' }}</td>
              </tr>
            </ng-template>
          </p-table>
        }
      </p-dialog>
    </div>
  `,
  styles: `
    .row-actions {
      display: flex;
      gap: 0.25rem;
      align-items: center;
    }
  `
})
export class HistoricoVendasComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly vendaService = inject(VendaService);
  private readonly messages = inject(MessageService);
  private readonly confirmation = inject(ConfirmationService);

  protected readonly vendas = signal<VendaResponse[]>([]);
  protected readonly vendaSelecionada = signal<VendaResponse | null>(null);
  protected readonly loading = signal(false);
  protected detalhesVisible = false;

  ngOnInit(): void {
    this.carregarVendas();
  }

  carregarVendas(): void {
    this.loading.set(true);
    this.vendaService.listarTodas().subscribe({
      next: (lista) => {
        this.vendas.set(lista);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
      }
    });
  }

  verDetalhes(v: VendaResponse): void {
    this.vendaSelecionada.set(v);
    this.detalhesVisible = true;
  }

  confirmarCancelamento(v: VendaResponse): void {
    this.confirmation.confirm({
      header: 'Cancelar Venda',
      message: `Deseja cancelar a Venda #${v.id} e estornar o estoque dos produtos?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sim, cancelar',
      rejectLabel: 'Não',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.vendaService.cancelar(v.id).subscribe({
          next: () => {
            this.messages.add({
              severity: 'success',
              summary: 'Venda Cancelada',
              detail: `Venda #${v.id} cancelada e estoque estornado.`
            });
            this.carregarVendas();
          },
          error: (err) =>
            this.messages.add({
              severity: 'error',
              summary: 'Erro ao cancelar',
              detail: extractApiErrorMessage(err)
            })
        });
      }
    });
  }
}

