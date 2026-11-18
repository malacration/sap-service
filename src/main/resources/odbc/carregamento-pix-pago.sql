-- PIX pago de cada pedido da ordem de carregamento, numa consulta so para todos os pedidos.
--
-- O PIX gerado pelo pedido vira um adiantamento (ODPI) com U_TX_DocTypeRef = 17 e U_TX_DocEntryRef = pedido.
-- A nota que sai do pedido tem que abater o saldo inteiro dele: a SBO_SP_VALIDACAO_FINANCEIRA (sap-sql-sovis)
-- recusa a nota se nao abater. A conta aqui e a mesma da trava:
--   pago (DocStatus 'C'), nao cancelado, sem devolucao (ORIN com base no adiantamento) e com saldo.
--   Saldo = total do adiantamento - o que notas nao canceladas ja abateram dele (INV9.DrawnSum).
-- Pelo sap-odbc porque o SQLQueries do Service Layer nao aceita SUM/subconsulta no SELECT.
SELECT A."Pedido", A."DocEntry", A."DocNum", A."Saldo"
FROM (
    SELECT
        dpi."U_TX_DocEntryRef" AS "Pedido",
        dpi."DocEntry",
        dpi."DocNum",
        dpi."DocTotal" - IFNULL((
            SELECT SUM(ap."DrawnSum")
            FROM INV9 ap
            INNER JOIN OINV nota ON nota."DocEntry" = ap."DocEntry"
            WHERE ap."BaseAbs" = dpi."DocEntry"
                AND nota."CANCELED" = 'N'
        ), 0) AS "Saldo"
    FROM ODPI dpi
    WHERE dpi."U_TX_DocTypeRef" = 17
        AND dpi."U_TX_DocEntryRef" IN (:pedidos)
        AND dpi."CANCELED" = 'N'
        AND dpi."DocStatus" = 'C'
        AND NOT EXISTS (
            SELECT 1
            FROM RIN1 dv
            INNER JOIN ORIN dev ON dev."DocEntry" = dv."DocEntry"
            WHERE dv."BaseType" = 203
                AND dv."BaseEntry" = dpi."DocEntry"
                AND dev."CANCELED" = 'N'
        )
) A
WHERE A."Saldo" > 0.01
ORDER BY A."Pedido", A."DocNum"
