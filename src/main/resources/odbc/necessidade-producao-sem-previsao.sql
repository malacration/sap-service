-- O que compoe o Sem previsao de um produto: contratos de venda futura com saldo e notas de
-- simples faturamento com entrega pendente. Mesma conta de CONTRATO_VF e SIMPLES em
-- necessidade-producao.sql, uma linha por documento (e deposito, na nota). Unidade de estoque.
--
-- Binds:
--   :itemCode     produto
--   :filiais      filiais escolhidas: a do contrato e a da nota de simples faturamento
--   :utilizacoes  utilizacoes de simples faturamento
WITH
CONTRATADO AS (
    SELECT linha."DocEntry" AS "DocEntry", SUM(linha."U_quantity" * item."NumInSale") AS "Qtd"
    FROM "@AR_CF_LINHA" linha
    INNER JOIN "@AR_CONTRATO_FUTURO" contrato ON contrato."DocEntry" = linha."DocEntry"
    INNER JOIN OITM item ON item."ItemCode" = linha."U_itemCode"
    WHERE
        contrato."U_status" = 'aberto'
        AND contrato."U_filial" IN (:filiais)
        AND linha."U_itemCode" = :itemCode
    GROUP BY linha."DocEntry"
),
ENTREGUE AS (
    SELECT nota."U_venda_futura" AS "DocEntry", SUM(linha."InvQty") AS "Qtd"
    FROM OINV nota
    INNER JOIN INV1 linha ON linha."DocEntry" = nota."DocEntry"
    WHERE
        nota."CANCELED" = 'N'
        AND nota."U_entrega_vf" = 1
        AND nota."U_venda_futura" IS NOT NULL
        AND linha."ItemCode" = :itemCode
    GROUP BY nota."U_venda_futura"
),
DEVOLVIDO AS (
    SELECT devolucao."U_venda_futura" AS "DocEntry", SUM(linha."InvQty") AS "Qtd"
    FROM ORIN devolucao
    INNER JOIN RIN1 linha ON linha."DocEntry" = devolucao."DocEntry"
    WHERE
        devolucao."CANCELED" = 'N'
        AND devolucao."U_venda_futura" IS NOT NULL
        AND linha."BaseType" <> 203
        AND linha."ItemCode" = :itemCode
    GROUP BY devolucao."U_venda_futura"
),
EM_PEDIDO AS (
    SELECT pedido."U_venda_futura" AS "DocEntry", SUM(linha."OpenInvQty") AS "Qtd"
    FROM ORDR pedido
    INNER JOIN RDR1 linha ON linha."DocEntry" = pedido."DocEntry"
    WHERE
        pedido."CANCELED" = 'N'
        AND linha."LineStatus" = 'O'
        AND pedido."U_venda_futura" IS NOT NULL
        AND linha."ItemCode" = :itemCode
    GROUP BY pedido."U_venda_futura"
),
CONTRATO_VF AS (
    SELECT
        contratado."DocEntry" AS "DocEntry",
        contratado."Qtd" AS "Contratado",
        IFNULL(entregue."Qtd", 0) - IFNULL(devolvido."Qtd", 0) AS "Entregue",
        IFNULL(em_pedido."Qtd", 0) AS "EmPedido",
        GREATEST(
            contratado."Qtd" - IFNULL(entregue."Qtd", 0) + IFNULL(devolvido."Qtd", 0) - IFNULL(em_pedido."Qtd", 0),
            0) AS "Saldo"
    FROM CONTRATADO contratado
    LEFT JOIN ENTREGUE entregue ON entregue."DocEntry" = contratado."DocEntry"
    LEFT JOIN DEVOLVIDO devolvido ON devolvido."DocEntry" = contratado."DocEntry"
    LEFT JOIN EM_PEDIDO em_pedido ON em_pedido."DocEntry" = contratado."DocEntry"
)
SELECT
    'VENDA_FUTURA'                                       AS "Tipo",
    contrato."DocEntry"                                  AS "DocEntry",
    contrato."DocEntry"                                  AS "DocNum",
    TO_VARCHAR(contrato."CreateDate", 'YYYY-MM-DD')      AS "Data",
    CAST(NULL AS NVARCHAR(10))                           AS "Previsao",
    contrato."U_cardCode"                                AS "CardCode",
    contrato."U_cardName"                                AS "CardName",
    IFNULL(contrato."U_filial", 0)                       AS "BPLId",
    filial."BPLName"                                     AS "BPLName",
    CAST(NULL AS NVARCHAR(254))                          AS "Deposito",
    vendedor."SlpName"                                   AS "Vendedor",
    contrato."DocEntry"                                  AS "ContratoVendaFutura",
    vf."Contratado"                                      AS "Quantidade",
    vf."Entregue"                                        AS "Entregue",
    vf."EmPedido"                                        AS "EmPedido",
    vf."Saldo"                                           AS "Saldo"
FROM CONTRATO_VF vf
INNER JOIN "@AR_CONTRATO_FUTURO" contrato ON contrato."DocEntry" = vf."DocEntry"
LEFT JOIN OBPL filial ON filial."BPLId" = contrato."U_filial"
LEFT JOIN OSLP vendedor ON vendedor."SlpCode" = contrato."U_vendedor"
WHERE vf."Saldo" > 0

UNION ALL

SELECT
    'SIMPLES_FATURAMENTO'                                AS "Tipo",
    nota."DocEntry"                                      AS "DocEntry",
    nota."DocNum"                                        AS "DocNum",
    TO_VARCHAR(nota."DocDate", 'YYYY-MM-DD')             AS "Data",
    TO_VARCHAR(nota."DocDueDate", 'YYYY-MM-DD')          AS "Previsao",
    nota."CardCode"                                      AS "CardCode",
    nota."CardName"                                      AS "CardName",
    IFNULL(nota."BPLId", 0)                              AS "BPLId",
    nota."BPLName"                                       AS "BPLName",
    linha."WhsCode"                                      AS "Deposito",
    vendedor."SlpName"                                   AS "Vendedor",
    nota."U_venda_futura"                                AS "ContratoVendaFutura",
    SUM(linha."InvQty")                                  AS "Quantidade",
    SUM(linha."InvQty" - linha."OpenInvQty")             AS "Entregue",
    0                                                    AS "EmPedido",
    SUM(linha."OpenInvQty")                              AS "Saldo"
FROM OINV nota
INNER JOIN INV1 linha ON linha."DocEntry" = nota."DocEntry"
LEFT JOIN OSLP vendedor ON vendedor."SlpCode" = nota."SlpCode"
WHERE
    nota."CANCELED" = 'N'
    AND linha."LineStatus" = 'O'
    AND linha."ItemCode" = :itemCode
    AND nota."BPLId" IN (:filiais)
    AND linha."Usage" IN (:utilizacoes)
GROUP BY
    nota."DocEntry", nota."DocNum", nota."DocDate", nota."DocDueDate", nota."CardCode",
    nota."CardName", nota."BPLId", nota."BPLName", vendedor."SlpName", nota."U_venda_futura", linha."WhsCode"

ORDER BY "Tipo" DESC, "Data", "DocNum"
