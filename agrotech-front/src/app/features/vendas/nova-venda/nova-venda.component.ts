import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { ClienteResponse } from '../../../core/models/cliente.model';
import { ProdutoResponse } from '../../../core/models/produto.model';
import { CartItem } from '../../../core/models/venda.model';
import { AuthService } from '../../../core/services/auth.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { ProdutoService } from '../../../core/services/produto.service';
import { VendaService } from '../../../core/services/venda.service';
import { extractApiErrorMessage } from '../../../core/utils/api-error.util';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-nova-venda',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    CurrencyPipe,
    RouterLink,
    PageHeaderComponent,
    SelectModule,
    InputNumberModule,
    ButtonModule,
    TableModule
  ],
  template: `
    <div class="page-container">
      <app-page-header
        title="Nova Venda"
        subtitle="Selecione o cliente, adicione produtos ao carrinho e finalize o pedido"
      >
        <p-button
          id="btn-voltar-vendas"
          label="Histórico"
          icon="pi pi-list"
          severity="secondary"
          [text]="true"
          routerLink="/vendas"
        />
      </app-page-header>

      <div class="surface-card">
        <form [formGroup]="cabecalhoForm" class="form-grid">
          <div class="field-group">
            <label for="select-venda-cliente">Cliente *</label>
            <p-select
              inputId="select-venda-cliente"
              formControlName="clienteId"
              [options]="clientes()"
              optionLabel="nome"
              optionValue="id"
              placeholder="Selecione o cliente"
            />
            @if (cabecalhoForm.controls.clienteId.invalid && cabecalhoForm.controls.clienteId.touched) {
              <small class="field-error">Selecione o cliente.</small>
            }
          </div>

          <div class="field-group">
            <label>Vendedor Logado</label>
            <div id="venda-vendedor-logado" class="readonly-field">
              {{ auth.currentUser()?.funcionarioNome }} ({{ auth.currentUser()?.username }})
            </div>
          </div>
        </form>
      </div>

      <div class="surface-card mt-md">
        <h2 class="section-title">Adicionar Item ao Carrinho</h2>
        <form [formGroup]="itemForm" (ngSubmit)="adicionarItem()" class="item-adder">
          <div class="field-group item-select">
            <label for="select-venda-produto">Produto *</label>
            <p-select
              inputId="select-venda-produto"
              formControlName="produtoId"
              [options]="produtosVenda()"
              optionLabel="nome"
              optionValue="id"
              optionDisabled="indisponivel"
              [filter]="true"
              filterBy="nome"
              placeholder="Selecione um produto"
            >
              <ng-template #item let-p>
                <div class="produto-option">
                  <span>{{ p.nome }}</span>
                  <small class="text-muted">
                    Estoque: {{ p.quantidadeEstoque }} | {{ p.precoAtual | currency: 'BRL' }}
                    @if (p.motivo) {
                      | <strong class="field-error">{{ p.motivo }}</strong>
                    }
                  </small>
                </div>
              </ng-template>
            </p-select>
          </div>

          <div class="field-group item-qty">
            <label for="input-venda-quantidade">Quantidade *</label>
            <p-inputnumber
              inputId="input-venda-quantidade"
              formControlName="quantidade"
              [min]="1"
              [showButtons]="true"
              [useGrouping]="false"
            />
          </div>

          <div class="item-btn">
            <p-button
              id="btn-adicionar-item"
              type="submit"
              label="Adicionar Item"
              icon="pi pi-cart-plus"
              severity="primary"
            />
          </div>
        </form>

        <div class="mt-lg">
          <p-table
            [value]="cartItems()"
            styleClass="p-datatable-sm p-datatable-striped"
          >
            <ng-template #header>
              <tr>
                <th>Produto</th>
                <th>Preço Unitário</th>
                <th>Quantidade</th>
                <th>Subtotal</th>
                <th style="width: 80px">Ação</th>
              </tr>
            </ng-template>
            <ng-template #body let-item>
              <tr>
                <td>{{ item.nomeProduto }}</td>
                <td>{{ item.precoUnitario | currency: 'BRL' }}</td>
                <td>{{ item.quantidade }}</td>
                <td>{{ item.precoUnitario * item.quantidade | currency: 'BRL' }}</td>
                <td>
                  <p-button
                    [id]="'btn-remover-item-' + item.produtoId"
                    icon="pi pi-trash"
                    [text]="true"
                    severity="danger"
                    (onClick)="removerItem(item.produtoId)"
                  />
                </td>
              </tr>
            </ng-template>
            <ng-template #emptymessage>
              <tr>
                <td colspan="5">Nenhum item adicionado ao carrinho.</td>
              </tr>
            </ng-template>
          </p-table>
        </div>

        <div class="checkout-footer">
          <div class="total-box">
            <span class="text-muted">Total da Venda</span>
            <strong id="venda-valor-total">{{ valorTotal() | currency: 'BRL' }}</strong>
          </div>

          <p-button
            id="btn-finalizar-venda"
            label="Finalizar Venda"
            icon="pi pi-check"
            severity="success"
            [loading]="submitting()"
            [disabled]="cartItems().length === 0"
            (onClick)="finalizarVenda()"
          />
        </div>
      </div>
    </div>
  `,
  styles: `
    .section-title {
      margin: 0 0 1rem;
      font-size: 1.05rem;
      font-weight: 600;
    }

    .readonly-field {
      padding: 0.65rem 0.75rem;
      background: var(--bg-page);
      border: 1px solid var(--border-subtle);
      border-radius: 6px;
      font-size: 0.925rem;
      color: var(--text-main);
    }

    .item-adder {
      display: flex;
      gap: 1rem;
      align-items: flex-end;
      flex-wrap: wrap;
    }

    .item-select {
      flex: 2 1 300px;
    }

    .item-qty {
      flex: 1 1 140px;
    }

    .item-btn {
      padding-bottom: 1px;
    }

    .produto-option {
      display: flex;
      flex-direction: column;
      gap: 0.15rem;
    }

    .checkout-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 1.5rem;
      padding-top: 1rem;
      border-top: 1px solid var(--border-subtle);
      flex-wrap: wrap;
      gap: 1rem;
    }

    .total-box {
      display: flex;
      flex-direction: column;
    }

    .total-box strong {
      font-size: 1.5rem;
      color: var(--primary-color);
    }
  `
})
export class NovaVendaComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  protected readonly auth = inject(AuthService);
  private readonly clienteService = inject(ClienteService);
  private readonly produtoService = inject(ProdutoService);
  private readonly vendaService = inject(VendaService);
  private readonly messages = inject(MessageService);
  private readonly router = inject(Router);

  protected readonly clientes = signal<ClienteResponse[]>([]);
  protected readonly produtos = signal<ProdutoResponse[]>([]);
  protected readonly cartItems = signal<CartItem[]>([]);
  protected readonly submitting = signal(false);

  /** Produtos com estoque zerado ou validade vencida não podem ser vendidos (RN02 / validade). */
  protected readonly produtosVenda = computed(() => {
    const hoje = new Date().toISOString().slice(0, 10);
    return this.produtos().map((p) => {
      const vencido = !!p.dataValidade && p.dataValidade < hoje;
      const motivo = vencido ? 'Vencido' : p.quantidadeEstoque <= 0 ? 'Sem estoque' : '';
      return { ...p, indisponivel: !!motivo, motivo };
    });
  });

  protected readonly valorTotal = computed(() =>
    this.cartItems().reduce((sum, item) => sum + item.precoUnitario * item.quantidade, 0)
  );

  protected readonly cabecalhoForm = this.fb.group({
    clienteId: [0, [Validators.required, Validators.min(1)]]
  });

  protected readonly itemForm = this.fb.group({
    produtoId: [0, [Validators.required, Validators.min(1)]],
    quantidade: [1, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    this.clienteService.listar().subscribe({
      next: (page) => this.clientes.set(page.content),
      error: (err) =>
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) })
    });

    this.produtoService.listar().subscribe({
      next: (page) => this.produtos.set(page.content),
      error: (err) =>
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) })
    });
  }

  adicionarItem(): void {
    if (this.itemForm.invalid) {
      this.itemForm.markAllAsTouched();
      this.messages.add({
        severity: 'warn',
        summary: 'Atenção',
        detail: 'Selecione um produto e informe uma quantidade válida.'
      });
      return;
    }

    const { produtoId, quantidade } = this.itemForm.getRawValue();
    const produto = this.produtos().find((p) => p.id === produtoId);
    if (!produto) return;

    const current = this.cartItems();
    const existing = current.find((i) => i.produtoId === produtoId);

    const jaNoCarrinho = existing?.quantidade ?? 0;
    if (jaNoCarrinho + quantidade > produto.quantidadeEstoque) {
      this.messages.add({
        severity: 'warn',
        summary: 'Estoque insuficiente',
        detail: `"${produto.nome}": disponível ${produto.quantidadeEstoque}, já no carrinho ${jaNoCarrinho}.`
      });
      return;
    }

    if (existing) {
      this.cartItems.set(
        current.map((i) =>
          i.produtoId === produtoId ? { ...i, quantidade: i.quantidade + quantidade } : i
        )
      );
    } else {
      this.cartItems.set([
        ...current,
        {
          produtoId: produto.id,
          nomeProduto: produto.nome,
          precoUnitario: produto.precoAtual,
          quantidade,
          estoqueDisponivel: produto.quantidadeEstoque
        }
      ]);
    }

    this.itemForm.reset({ produtoId: 0, quantidade: 1 });
  }

  removerItem(produtoId: number): void {
    this.cartItems.set(this.cartItems().filter((i) => i.produtoId !== produtoId));
  }

  finalizarVenda(): void {
    if (this.cabecalhoForm.invalid) {
      this.cabecalhoForm.markAllAsTouched();
      this.messages.add({
        severity: 'warn',
        summary: 'Cliente obrigatório',
        detail: 'Selecione um cliente para finalizar a venda.'
      });
      return;
    }

    const funcionarioId = this.auth.currentUser()?.funcionarioId;
    if (!funcionarioId || this.cartItems().length === 0) {
      return;
    }

    this.submitting.set(true);
    this.vendaService
      .efetivar({
        clienteId: this.cabecalhoForm.getRawValue().clienteId,
        funcionarioId,
        itens: this.cartItems().map((item) => ({
          produtoId: item.produtoId,
          quantidade: item.quantidade
        }))
      })
      .subscribe({
        next: (venda) => {
          this.submitting.set(false);
          this.messages.add({
            severity: 'success',
            summary: 'Venda Concluída',
            detail: `Venda #${venda.id} efetivada com sucesso!`
          });
          this.router.navigate(['/vendas']);
        },
        error: (err) => {
          this.submitting.set(false);
          this.messages.add({
            severity: 'error',
            summary: 'Não foi possível concluir a venda',
            detail: extractApiErrorMessage(err)
          });
        }
      });
  }
}

