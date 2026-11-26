-- Cauda da base painel-carregamento-linhas.sql: totais do filtro (sem agrupar nem paginar).
-- Sem quantidade: soma de unidades diferentes nao significa nada.
SELECT
    COUNT(*) AS "Linhas",
    COUNT(DISTINCT CASE WHEN "Tipo" IS NULL THEN "DocEntry" END) AS "Pedidos",
    COUNT(DISTINCT CASE WHEN "Tipo" = 'NF venda futura' THEN "DocEntry" END) AS "NotasVendaFutura"
FROM FILTRADAS
