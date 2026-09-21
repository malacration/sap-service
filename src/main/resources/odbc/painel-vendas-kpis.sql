-- KPIs consolidados do painel de vendas.
--
-- UMA consulta para todos os cartoes: chamadas independentes varreriam OINV/ORIN
-- varias vezes por troca de filtro e poderiam degradar o ERP.
--
-- REGRA DE CANCELAMENTO (nao retroage): 'N'/'Y' entram com o sinal da propria
-- tabela na data do documento original; 'C' entra com sinal OPOSTO, na data do
-- cancelamento. NAO filtrar CANCELED='N': e ele que faz a venda sumir do mes.
--
-- Filtros condicionais: :todasFiliais = 1 e :todosVendedores = 1 desligam a
-- restricao correspondente (admin). A lista :filiais nunca vai vazia - `IN ()`
-- e erro de sintaxe -, por isso o flag e que desliga, nao a lista.
--
-- IMPOSTOS: NAO existe aqui um "faturamento sem impostos". `DocTotal - VatSum`
-- nao serve no Brasil - o sistema tributario nao e um IVA unico. PIS/COFINS e
-- ICMS sao "por dentro" (embutidos no preco), enquanto IPI e ICMS-ST sao "por
-- fora". Some-se a isso o ICMS desonerado desta base, onde o LineTotal e
-- MAJORADO (ver DesoneradoService) e deixa de representar o valor combinado.
-- Calcular receita liquida exige tratar INV4 por tipo de tributo. Ate essa regra
-- existir, o painel publica apenas o faturamento BRUTO, que e verificavel.
--
-- SERVICOS: os KPIs de DOCUMENTO (faturamento, ticket, notas) incluem
-- DocType='S'. Ja o bloco de QUANTIDADE junta com OITM e portanto cobre so
-- linhas de produto - e por isso que preco medio e faturamento tem bases
-- diferentes, e a tela precisa dizer isso.
WITH DOCS AS (
    SELECT
        'I' AS ORIGEM,
        F."CardCode" AS CARD_CODE,
        F."BPLId" AS FILIAL,
        CASE WHEN F."CANCELED" = 'C' THEN -1 ELSE 1 END AS SINAL,
        F."DocTotal" AS TOTAL_BRUTO,
        F."VatSum" AS IMPOSTO
    FROM OINV F
    WHERE F."DocDate" >= :dataInicio
      AND F."DocDate" <= :dataFim
      AND (:todasFiliais = 1 OR F."BPLId" IN (:filiais))
      AND (:todosVendedores = 1 OR F."SlpCode" = :vendedor)
    UNION ALL
    SELECT
        'R',
        D."CardCode",
        D."BPLId",
        CASE WHEN D."CANCELED" = 'C' THEN 1 ELSE -1 END,
        D."DocTotal",
        D."VatSum"
    FROM ORIN D
    WHERE D."DocDate" >= :dataInicio
      AND D."DocDate" <= :dataFim
      AND (:todasFiliais = 1 OR D."BPLId" IN (:filiais))
      AND (:todosVendedores = 1 OR D."SlpCode" = :vendedor)
),
-- Linhas de PRODUTO, para o preco medio. O INNER JOIN com OITM e o que descarta
-- documentos de servico, que tem linha em INV1 mas nao tem item de verdade.
-- InvQty (UM de estoque), nunca Quantity (UM de venda): com multiplas UMs por
-- item o mesmo produto ficaria incomparavel consigo mesmo.
LINHAS AS (
    SELECT
        CASE WHEN F."CANCELED" = 'C' THEN -1 ELSE 1 END AS SINAL,
        L."LineTotal" AS VALOR,
        L."InvQty" AS QTD
    FROM OINV F
    INNER JOIN INV1 L ON L."DocEntry" = F."DocEntry"
    INNER JOIN OITM I ON I."ItemCode" = L."ItemCode"
    WHERE F."DocDate" >= :dataInicio
      AND F."DocDate" <= :dataFim
      AND (:todasFiliais = 1 OR F."BPLId" IN (:filiais))
      AND (:todosVendedores = 1 OR F."SlpCode" = :vendedor)
    UNION ALL
    SELECT
        CASE WHEN D."CANCELED" = 'C' THEN 1 ELSE -1 END,
        L."LineTotal",
        L."InvQty"
    FROM ORIN D
    INNER JOIN RIN1 L ON L."DocEntry" = D."DocEntry"
    INNER JOIN OITM I ON I."ItemCode" = L."ItemCode"
    WHERE D."DocDate" >= :dataInicio
      AND D."DocDate" <= :dataFim
      AND (:todasFiliais = 1 OR D."BPLId" IN (:filiais))
      AND (:todosVendedores = 1 OR D."SlpCode" = :vendedor)
),
SALDO_CLIENTE AS (
    SELECT CARD_CODE, SUM(CASE WHEN ORIGEM = 'I' THEN SINAL ELSE 0 END) AS QTD
    FROM DOCS GROUP BY CARD_CODE
),
SALDO_FILIAL AS (
    SELECT FILIAL, SUM(CASE WHEN ORIGEM = 'I' THEN SINAL ELSE 0 END) AS QTD
    FROM DOCS GROUP BY FILIAL
)
SELECT
    SUM(CASE WHEN ORIGEM = 'I' THEN SINAL * TOTAL_BRUTO ELSE 0 END) AS FATURAMENTO_BRUTO,
    SUM(CASE WHEN ORIGEM = 'R' THEN -1 * SINAL * TOTAL_BRUTO ELSE 0 END) AS DEVOLUCOES,
    SUM(SINAL * TOTAL_BRUTO) AS FATURAMENTO_LIQUIDO,
    SUM(CASE WHEN ORIGEM = 'I' THEN SINAL ELSE 0 END) AS QTD_FATURAS,
    SUM(CASE WHEN ORIGEM = 'R' THEN -1 * SINAL ELSE 0 END) AS QTD_DEVOLUCOES,
    (SELECT COUNT(*) FROM SALDO_CLIENTE WHERE QTD > 0) AS CLIENTES_ATIVOS,
    (SELECT COUNT(*) FROM SALDO_FILIAL WHERE QTD > 0) AS FILIAIS_COM_VENDA,
    (SELECT SUM(SINAL * VALOR) FROM LINHAS) AS VALOR_PRODUTOS,
    (SELECT SUM(SINAL * QTD) FROM LINHAS) AS QTD_ITENS
FROM DOCS
