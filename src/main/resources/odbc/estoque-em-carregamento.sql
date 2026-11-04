-- Quantidade de cada item reservada em ordens de carregamento Abertas, de uma vez para a tela
-- (antes era uma chamada por item): linhas de pedido vinculadas pela RDR1.U_ORD_CARREGAMENTO
-- (so o que falta entregar) + retiradas de NF de venda futura na @ORD_CRG_LINHA.
SELECT "ItemCode", SUM("Quantidade") AS "Quantidade"
FROM (
    SELECT L."ItemCode", L."OpenQty" AS "Quantidade"
    FROM "RDR1" L
    INNER JOIN "@ORD_CARREGAMENTO" D ON L."U_ORD_CARREGAMENTO" = D."DocEntry"
    WHERE D."U_Status" = 'Aberto' AND L."LineStatus" = 'O'
    UNION ALL
    SELECT R."U_itemCode", R."U_qtdRetirada"
    FROM "@ORD_CRG_LINHA" R
    INNER JOIN "@ORD_CARREGAMENTO" D ON D."DocEntry" = R."DocEntry"
    WHERE D."U_Status" = 'Aberto' AND R."U_baseType" = 13
) T
GROUP BY "ItemCode"
