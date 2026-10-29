-- Rastreio de lote: tudo o que as ordens de producao ligadas aos lotes pedidos fizeram com cada lote,
-- liquido de estorno e devolucao. O rastreio abre um nivel por clique (ver RastreioLoteService).
--
-- Vem do log de lotes (OITL/ITL1/OBTN), nao das linhas da OP: e ali que o SAP guarda QUAL lote
-- entrou e saiu. BaseType 202 = documento gerado a partir da OP; DocType 59 = entrada de producao,
-- 60 = saida para producao. StockEff 1 = movimento de estoque (2 e so a alocacao, duplicada).
--
-- Papel (P = lote gerado pela OP, C = lote consumido) vem do item e nao do tipo de documento:
--  - OP normal: o produto da OP e gerado; qualquer outro item e componente. Componente devolvido
--    entra como 59, mas e devolucao - soma com o sinal dele e abate o consumo.
--  - Desmontagem (Type D): ao contrario - o produto entra, os componentes saem.
-- Quantidade liquida: positiva quando gerou, negativa quando consumiu. Lote totalmente estornado ou
-- devolvido some (HAVING).
--
-- ALVO escolhe as OPs, as :limite mais novas, e depois traz TODOS os movimentos delas:
--  - :lotesAcima: as que geraram algum desses lotes ("ITEM|LOTE") - a origem;
--  - :lotesAbaixo: as que consumiram algum desses lotes - o destino;
--  - :loteGerado: as que geraram o lote digitado pelo usuario, que nao tem item;
--  - :loteConsumido: as que consumiram o lote digitado - so para achar o item de um lote comprado,
--    que nenhuma OP gerou (o servico pede com :limite 1).
-- O numero do lote (DistNumber) so e unico junto do item: o mesmo texto ("repro") aparece em itens
-- diferentes, por isso as listas levam o par. :textosAcima/:textosAbaixo repetem so o numero: nao muda o resultado,
-- mas deixa o banco usar o indice do lote antes de comparar o par (sem ele a consulta dobra de tempo).
-- Parametro sem uso vai com um valor que nao casa (lista nunca vai vazia).
WITH MOVIMENTO AS (
    SELECT
        L."BaseEntry" AS "Ordem",
        L."ItemCode"  AS "Item",
        B."DistNumber" AS "Lote",
        CASE WHEN W."Type" = 'D'
             THEN (CASE WHEN L."ItemCode" = W."ItemCode" THEN 'C' ELSE 'P' END)
             ELSE (CASE WHEN L."ItemCode" = W."ItemCode" THEN 'P' ELSE 'C' END)
        END AS "Papel",
        T."Quantity" AS "Quantidade"
    FROM "OITL" L
    JOIN "ITL1" T ON T."LogEntry" = L."LogEntry"
    JOIN "OBTN" B ON B."AbsEntry" = T."MdAbsEntry"
    JOIN "OWOR" W ON W."DocEntry" = L."BaseEntry"
    WHERE L."BaseType" = 202
      AND L."DocType" IN (59, 60)
      AND L."StockEff" = 1
),
ALVO AS (
    SELECT DISTINCT M."Ordem"
    FROM MOVIMENTO M
    WHERE (M."Papel" = 'P' AND M."Lote" IN (:textosAcima) AND M."Item" || '|' || M."Lote" IN (:lotesAcima))
       OR (M."Papel" = 'C' AND M."Lote" IN (:textosAbaixo) AND M."Item" || '|' || M."Lote" IN (:lotesAbaixo))
       OR (M."Papel" = 'P' AND M."Lote" = :loteGerado)
       OR (M."Papel" = 'C' AND M."Lote" = :loteConsumido)
    ORDER BY M."Ordem" DESC
    LIMIT :limite
)
SELECT
    M."Ordem"        AS "Ordem",
    W."DocNum"       AS "OrdemNumero",
    W."ItemCode"     AS "OrdemItem",
    W."ProdName"     AS "OrdemItemNome",
    W."Status"       AS "OrdemStatus",
    W."Type"         AS "OrdemTipo",
    W."PostDate"     AS "OrdemData",
    M."Item"         AS "Item",
    I."ItemName"     AS "ItemNome",
    M."Lote"         AS "Lote",
    M."Papel"        AS "Papel",
    SUM(M."Quantidade") AS "Quantidade"
FROM MOVIMENTO M
JOIN ALVO A ON A."Ordem" = M."Ordem"
JOIN "OWOR" W ON W."DocEntry" = M."Ordem"
LEFT JOIN "OITM" I ON I."ItemCode" = M."Item"
GROUP BY M."Ordem", W."DocNum", W."ItemCode", W."ProdName", W."Status", W."Type", W."PostDate",
         M."Item", I."ItemName", M."Lote", M."Papel"
HAVING SUM(M."Quantidade") <> 0
