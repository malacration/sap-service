-- Rastreio de lote, destino final: documentos de venda que tiraram (ou devolveram) os lotes pedidos do
-- estoque, com o cliente. Vem do log de lotes (OITL/ITL1/OBTN), como rastreio-lote-movimentos.sql.
--
-- DocType 15 = entrega, 13 = nota fiscal de saida, 16 = devolucao, 14 = devolucao de nota. Nota copiada
-- de uma entrega nao mexe no estoque de novo - so a entrega aparece (StockEff 1 = movimento de estoque).
-- Quantidade liquida por documento: negativa quando saiu para o cliente, positiva quando voltou.
--
-- :lotes leva "ITEM|LOTE" (o numero do lote so e unico junto do item); :textos repete so o numero
-- para o banco usar o indice do lote.
SELECT
    L."DocType"        AS "Tipo",
    L."DocEntry"       AS "DocEntry",
    L."DocNum"         AS "DocNum",
    L."DocDate"        AS "Data",
    L."CardCode"       AS "Cliente",
    L."CardName"       AS "ClienteNome",
    L."ItemCode"       AS "Item",
    B."DistNumber"     AS "Lote",
    SUM(T."Quantity")  AS "Quantidade"
FROM "OITL" L
JOIN "ITL1" T ON T."LogEntry" = L."LogEntry"
JOIN "OBTN" B ON B."AbsEntry" = T."MdAbsEntry"
WHERE L."DocType" IN (13, 14, 15, 16)
  AND L."StockEff" = 1
  AND B."DistNumber" IN (:textos)
  AND L."ItemCode" || '|' || B."DistNumber" IN (:lotes)
GROUP BY L."DocType", L."DocEntry", L."DocNum", L."DocDate", L."CardCode", L."CardName", L."ItemCode", B."DistNumber"
HAVING SUM(T."Quantity") <> 0
ORDER BY L."DocDate", L."DocNum"
