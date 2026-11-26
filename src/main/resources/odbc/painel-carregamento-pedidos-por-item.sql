-- Cauda da base painel-carregamento-linhas.sql: pedidos e NF agrupados por item, paginados POR GRUPO.
-- "Linha" e a posicao do grupo ("ItemCode") e "Total" o numero de grupos: uma pagina pode ter mais linhas que o tamanho.
-- Estoque soma uma vez por deposito: agrega por deposito (MAX, o saldo do item no deposito) e depois soma.
-- Tipo fica na chave: pedido e NF de venda futura nunca se misturam.
,
POR_DEPOSITO AS (
    SELECT "ItemCode", "Description", "EstoqueMinimo", "Tipo", "EmOrdemDeCarregamento", "WhsCode", SUM("Quantity") AS "Quantity", MAX("OnHand") AS "OnHand"
    FROM FILTRADAS
    GROUP BY "ItemCode", "Description", "EstoqueMinimo", "Tipo", "EmOrdemDeCarregamento", "WhsCode"
),
AGRUPADAS AS (
    SELECT "ItemCode", "Description", "EstoqueMinimo", "Tipo", "EmOrdemDeCarregamento", SUM("Quantity") AS "Quantity", SUM("OnHand") AS "OnHand"
    FROM POR_DEPOSITO
    GROUP BY "ItemCode", "Description", "EstoqueMinimo", "Tipo", "EmOrdemDeCarregamento"
),
NUMERADAS AS (
    SELECT AGRUPADAS.*, DENSE_RANK() OVER (ORDER BY "ItemCode") AS "Linha"
    FROM AGRUPADAS
),
TOTALIZADAS AS (
    SELECT NUMERADAS.*, MAX("Linha") OVER () AS "Total"
    FROM NUMERADAS
)
SELECT *
FROM TOTALIZADAS
WHERE "Linha" > :inicio AND "Linha" <= :fim
ORDER BY "ItemCode", "Description", "EstoqueMinimo", "Tipo", "EmOrdemDeCarregamento"
