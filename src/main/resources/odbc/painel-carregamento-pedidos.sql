-- Cauda da base painel-carregamento-linhas.sql: pedidos e NF sem agrupamento, uma pagina por vez.
-- Paginacao por ROW_NUMBER; COUNT(*) OVER () traz o total em toda linha (como nota-fiscal-lote.sql).
-- A ordenacao desempata por tudo que identifica a linha, senao a pagina muda entre chamadas.
,
NUMERADAS AS (
    SELECT
        FILTRADAS.*,
        ROW_NUMBER() OVER (ORDER BY "DocDate" DESC, "Tipo", "DocEntry" DESC, "LineNum", "EmOrdemDeCarregamento") AS "Linha",
        COUNT(*) OVER () AS "Total"
    FROM FILTRADAS
)
SELECT *
FROM NUMERADAS
WHERE "Linha" > :inicio AND "Linha" <= :fim
ORDER BY "Linha"
