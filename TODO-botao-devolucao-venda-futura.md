# TODO — Botão "Liberar nota para devolução" (Venda Futura)

Documentação de pendências levantadas na revisão do botão que libera a nota
fiscal para devolução. **Nada aqui foi corrigido ainda** — é um registro para
ajuste posterior.

## Fluxo atual

| Camada | Arquivo | Ponto |
|--------|---------|-------|
| Front (botão) | `front-sap/src/app/sap/components/venda-futura/single/single.component.ts` | `desfazerConcilicao()` (linha ~241) |
| Front (service) | `front-sap/src/app/sap/service/venda-futura.service.ts` | `cancelarConciliacao()` (linha ~80) |
| Backend (endpoint) | `sap-rovema/src/main/kotlin/br/andrew/sap/controllers/ContratoVendaFuturaController.kt` | `GET /contrato-venda-futura/devolver/{docEntry}` → `devolver()` (linha ~154) |
| Query | `reconciliacao-interna-document.sql` | conciliações internas da nota |

### O que o botão faz hoje
1. Busca as conciliações internas não-canceladas da nota (obj `13`).
2. Exige **exatamente uma** (senão lança exceção).
3. Marca a nota com `U_conciliar_automatico = 0`.
4. Cancela a reconciliação (reabre a nota).
5. Front avisa "Finalize a devolução diretamente no SAP B1".

Ou seja: o botão **apenas destrava a nota**. Ele desfaz a conciliação da
entrega com o lançamento de reclassificação (VFET) e delega o resto para um
passo manual no SAP B1 + schedules.

---

## Pendências

### 1. Estorno do adiantamento não é feito pelo botão (principal)
Na entrega, o `ConciliacaoVendaFuturaSchedule` cria uma nota de apropriação do
adiantamento (`U_TX_DocEntryRef = invoice.docEntry`), concilia com o lançamento
e muda o transcode do diário para `VFEC`. O botão **não reverte nada disso**.

O estorno só ocorre em `ReclassificacaoEntregaVendaFuturaSchedule.estorno()`
(linha ~154), que **só dispara depois** que o usuário cria a nota de crédito
(ORIN) no SAP B1 com `U_conciliar_automatico = 1`. Entre clicar no botão e criar
a devolução, o adiantamento continua consumido.

- [ ] Decidir se o botão deve estornar/reabrir o adiantamento, ou se isso
      permanece delegado ao schedule via criação da nota de crédito.

### 2. Ordem sem atomicidade / rollback
Grava `U_conciliar_automatico = 0` **antes** de cancelar a reconciliação
(`ContratoVendaFuturaController.kt:162-164`). Se o `serviceCancel` falhar, a nota
fica marcada com `0` (fora do schedule de reclassificação) mas **ainda
conciliada** → estado inconsistente e sem retry.

- [ ] Cancelar a reconciliação **primeiro** e só então marcar o flag, ou reverter
      o flag no `catch`.

### 3. `size != 1` frágil — falta `DISTINCT` no SQL
`reconciliacao-interna-document.sql` retorna **uma linha por linha da ITR1**, sem
`DISTINCT`. Se a nota participa da reconciliação com mais de uma linha/parcela,
vêm `ReconNum` repetidos, `recomNums.size` fica > 1 e o botão lança
"Nao pode existir mais de uma reconciliacao" indevidamente.

- [ ] Adicionar `DISTINCT` no SELECT.
- [ ] Cancelar cada `ReconNum` distinto num loop, em vez de assumir um só.

### 4. Não é idempotente
Clicar duas vezes (ou numa nota já liberada) cai em `recomNums.isEmpty()` →
lança "Nao existe conciliacao". A segunda chamada quebra em vez de ser no-op.

- [ ] Tratar "já liberada" como sucesso/no-op.

### 5. Sem validação de domínio
Não confere `U_venda_futura` / `U_entrega_vf`. O endpoint aceita qualquer
`docEntry` de nota e mexe na conciliação de documentos que não deveria.

- [ ] Validar que a nota é uma entrega de venda futura antes de agir.

### 6. Conflito com o gatilho de criação da devolução no SAP
Ao copiar a nota de crédito da fatura no SAP B1, o `U_conciliar_automatico`
costuma vir copiado da fatura — que o botão acabou de setar para **0**. Aí o
`SBO_SP_VALIDACAO_VENDA_FUTURA` bloqueia o `Add` do ORIN (obj `14`) com
"Modifique o campo Conciliar automaticamente para SIM". O usuário precisa lembrar
de virar para SIM manualmente.

- [ ] Orientar o usuário no fluxo (UI) ou preparar o campo, para não travar a
      criação da devolução.

---

## Prioridade sugerida
1. **#2** (ordem/rollback) e **#3** (`DISTINCT` + loop) — correções seguras e de
   maior impacto imediato.
2. **#4** (idempotência) e **#5** (validação de domínio).
3. **#1** e **#6** — dependem de decisão de fluxo/negócio.
