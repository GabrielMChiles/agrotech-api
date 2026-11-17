import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { FornecedorResponse, ProdutoResponse } from '../../core/models/produto.model';
import { ProdutoService } from '../../core/services/produto.service';
import { extractApiErrorMessage } from '../../core/utils/api-error.util';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-produtos',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    DatePipe,
    PageHeaderComponent,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    InputNumberModule,
    SelectModule
  ],
  template: `
    <div class="page-container">
      <app-page-header
        title="Produtos"
        subtitle="Gerencie insumos agrícolas, preços, estoque e fornecedores"
      >
        <p-button
          id="btn-novo-produto"
          label="Novo Produto"
          icon="pi pi-plus"
          severity="primary"
          (onClick)="abrirNovo()"
        />
      </app-page-header>

      <div class="surface-card">
        <p-table
          [value]="produtos()"
          [paginator]="true"
          [rows]="10"
          [loading]="loading()"
          styleClass="p-datatable-sm p-datatable-striped"
        >
          <ng-template #header>
            <tr>
              <th>Nome</th>
              <th>Categoria</th>
              <th>Fornecedor</th>
              <th>Preço</th>
              <th>Estoque</th>
              <th>Validade</th>
              <th style="width: 120px">Ações</th>
            </tr>
          </ng-template>
          <ng-template #body let-p>
            <tr>
              <td>{{ p.nome }}</td>
              <td>{{ p.categoria || '-' }}</td>
              <td>{{ p.nomeFornecedor }}</td>
              <td>{{ p.precoAtual | currency: 'BRL' }}</td>
              <td>{{ p.quantidadeEstoque }}</td>
              <td>{{ p.dataValidade ? (p.dataValidade | date: 'dd/MM/yyyy') : '-' }}</td>
              <td>
                <div class="row-actions">
                  <p-button
                    [id]="'btn-editar-produto-' + p.id"
                    icon="pi pi-pencil"
                    [text]="true"
                    severity="info"
                    (onClick)="abrirEdicao(p)"
                  />
                  <p-button
                    [id]="'btn-excluir-produto-' + p.id"
                    icon="pi pi-trash"
                    [text]="true"
                    severity="danger"
                    (onClick)="confirmarExclusao(p)"
                  />
                </div>
              </td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr>
              <td colspan="7">Nenhum produto cadastrado.</td>
            </tr>
          </ng-template>
        </p-table>
      </div>

      <p-dialog
        [(visible)]="dialogVisible"
        [header]="editandoId() ? 'Editar Produto' : 'Novo Produto'"
        [modal]="true"
        [style]="{ width: '640px', maxWidth: '95vw' }"
      >
        <form [formGroup]="form" (ngSubmit)="salvar()" class="dialog-form">
          <div class="form-grid">
            <div class="field-group">
              <label for="input-produto-nome">Nome *</label>
              <input
                id="input-produto-nome"
                type="text"
                pInputText
                formControlName="nome"
                placeholder="Ex: Semente de Soja 40kg"
              />
              @if (form.controls.nome.invalid && form.controls.nome.touched) {
                <small class="field-error">Nome é obrigatório.</small>
              }
            </div>

            <div class="field-group">
              <label for="input-produto-categoria">Categoria</label>
              <input
                id="input-produto-categoria"
                type="text"
                pInputText
                formControlName="categoria"
                placeholder="Ex: Sementes, Fertilizantes"
              />
            </div>

            <div class="field-group">
              <label for="input-produto-preco">Preço Atual (R$) *</label>
              <p-inputnumber
                inputId="input-produto-preco"
                formControlName="precoAtual"
                mode="currency"
                currency="BRL"
                locale="pt-BR"
                [min]="0.01"
              />
              @if (form.controls.precoAtual.invalid && form.controls.precoAtual.touched) {
                <small class="field-error">Informe um preço maior que zero.</small>
              }
            </div>

            <div class="field-group">
              <label for="input-produto-estoque">Quantidade em Estoque *</label>
              <p-inputnumber
                inputId="input-produto-estoque"
                formControlName="quantidadeEstoque"
                [useGrouping]="false"
                [min]="0"
              />
              @if (form.controls.quantidadeEstoque.invalid && form.controls.quantidadeEstoque.touched) {
                <small class="field-error">Estoque deve ser zero ou maior.</small>
              }
            </div>

            <div class="field-group">
              <label for="input-produto-validade">Data de Validade</label>
              <input
                id="input-produto-validade"
                type="date"
                pInputText
                formControlName="dataValidade"
              />
            </div>

            <div class="field-group">
              <label for="select-produto-fornecedor">Fornecedor *</label>
              <p-select
                inputId="select-produto-fornecedor"
                formControlName="fornecedorId"
                [options]="fornecedores()"
                optionLabel="razaoSocial"
                optionValue="id"
                placeholder="Selecione o fornecedor"
                appendTo="body"
              />
              @if (form.controls.fornecedorId.invalid && form.controls.fornecedorId.touched) {
                <small class="field-error">Selecione um fornecedor.</small>
              }
            </div>
          </div>

          <div class="actions-bar">
            <p-button
              id="btn-cancelar-produto"
              type="button"
              label="Cancelar"
              severity="secondary"
              [text]="true"
              (onClick)="dialogVisible = false"
            />
            <p-button
              id="btn-salvar-produto"
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
export class ProdutosComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly produtoService = inject(ProdutoService);
  private readonly messages = inject(MessageService);
  private readonly confirmation = inject(ConfirmationService);

  protected readonly produtos = signal<ProdutoResponse[]>([]);
  protected readonly fornecedores = signal<FornecedorResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected dialogVisible = false;

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required]],
    categoria: [''],
    precoAtual: [0.01, [Validators.required, Validators.min(0.01)]],
    quantidadeEstoque: [0, [Validators.required, Validators.min(0)]],
    dataValidade: [''],
    fornecedorId: [0, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    this.loading.set(true);
    this.produtoService.listar().subscribe({
      next: (page) => {
        this.produtos.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
      }
    });

    this.produtoService.listarFornecedores().subscribe({
      next: (page) => this.fornecedores.set(page.content),
      error: (err) =>
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) })
    });
  }

  abrirNovo(): void {
    this.editandoId.set(null);
    this.form.reset({
      nome: '',
      categoria: '',
      precoAtual: 1,
      quantidadeEstoque: 0,
      dataValidade: '',
      fornecedorId: 0
    });
    this.dialogVisible = true;
  }

  abrirEdicao(p: ProdutoResponse): void {
    this.editandoId.set(p.id);
    this.form.reset({
      nome: p.nome,
      categoria: p.categoria ?? '',
      precoAtual: p.precoAtual,
      quantidadeEstoque: p.quantidadeEstoque,
      dataValidade: p.dataValidade ?? '',
      fornecedorId: p.fornecedorId
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
      categoria: raw.categoria.trim() || null,
      precoAtual: raw.precoAtual,
      quantidadeEstoque: raw.quantidadeEstoque,
      dataValidade: raw.dataValidade || null,
      fornecedorId: raw.fornecedorId
    };

    this.saving.set(true);
    const id = this.editandoId();
    const request$ = id
      ? this.produtoService.atualizar(id, payload)
      : this.produtoService.criar(payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogVisible = false;
        this.messages.add({
          severity: 'success',
          summary: 'Sucesso',
          detail: id ? 'Produto atualizado com sucesso.' : 'Produto cadastrado com sucesso.'
        });
        this.carregarDados();
      },
      error: (err) => {
        this.saving.set(false);
        this.messages.add({ severity: 'error', summary: 'Erro', detail: extractApiErrorMessage(err) });
      }
    });
  }

  confirmarExclusao(p: ProdutoResponse): void {
    this.confirmation.confirm({
      header: 'Confirmar Exclusão',
      message: `Deseja excluir o produto "${p.nome}"?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.produtoService.excluir(p.id).subscribe({
          next: () => {
            this.messages.add({
              severity: 'success',
              summary: 'Excluído',
              detail: 'Produto removido com sucesso.'
            });
            this.carregarDados();
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

