-- Pedidos de venda em aberto de um produto: o detalhe da coluna Pedido da necessidade de producao.
-- Mesmo recorte do PEDIDO de necessidade-producao.sql, uma linha por documento e deposito.
--
-- Binds:
--   :itemCode     produto
--   :filiais      filiais escolhidas: a filial do pedido
--   :utilizacoes  utilizacoes de simples faturamento, que ficam de fora
SELECT
    'PEDIDO'                                             AS "Tipo",
    pedido."DocEntry"                                    AS "DocEntry",
    pedido."DocNum"                                      AS "DocNum",
    TO_VARCHAR(pedido."DocDate", 'YYYY-MM-DD')           AS "Data",
    TO_VARCHAR(pedido."DocDueDate", 'YYYY-MM-DD')        AS "Previsao",
    pedido."CardCode"                                    AS "CardCode",
    pedido."CardName"                                    AS "CardName",
    IFNULL(pedido."BPLId", 0)                            AS "BPLId",
    pedido."BPLName"                                     AS "BPLName",
    linha."WhsCode"                                      AS "Deposito",
    vendedor."SlpName"                                   AS "Vendedor",
    pedido."U_venda_futura"                              AS "ContratoVendaFutura",
    SUM(linha."InvQty")                                  AS "Quantidade",
    SUM(linha."InvQty" - linha."OpenInvQty")             AS "Entregue",
    0                                                    AS "EmPedido",
    SUM(linha."OpenInvQty")                              AS "Saldo"
FROM ORDR pedido
INNER JOIN RDR1 linha ON linha."DocEntry" = pedido."DocEntry"
LEFT JOIN OSLP vendedor ON vendedor."SlpCode" = pedido."SlpCode"
WHERE
    pedido."CANCELED" = 'N'
    AND linha."LineStatus" = 'O'
    AND linha."ItemCode" = :itemCode
    AND pedido."BPLId" IN (:filiais)
    AND IFNULL(linha."Usage", -1) NOT IN (:utilizacoes)
GROUP BY
    pedido."DocEntry", pedido."DocNum", pedido."DocDate", pedido."DocDueDate", pedido."CardCode",
    pedido."CardName", pedido."BPLId", pedido."BPLName", vendedor."SlpName", pedido."U_venda_futura", linha."WhsCode"
ORDER BY pedido."DocDueDate", pedido."DocNum"
