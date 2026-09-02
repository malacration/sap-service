SELECT SUM(X."Valor") AS "TotalFreteFaturado"
FROM (
    SELECT SUM(E."U_frete_negociado") AS "Valor"
    FROM OINV N
        INNER JOIN INV3 E ON E."DocEntry" = N."DocEntry" AND E."ExpnsCode" = 1
    WHERE N."CANCELED" = 'N'
        AND N."U_venda_futura" = :idContrato
        AND N."U_entrega_vf" = '1'
        AND E."U_frete_negociado" > 0

    UNION ALL

    SELECT SUM(E."LineTotal") AS "Valor"
    FROM OINV N
        INNER JOIN INV3 E ON E."DocEntry" = N."DocEntry" AND E."ExpnsCode" = 1
    WHERE N."CANCELED" = 'N'
        AND N."U_venda_futura" = :idContrato
        AND N."U_entrega_vf" = '1'
        AND (E."U_frete_negociado" IS NULL OR E."U_frete_negociado" <= 0)

    UNION ALL

    SELECT SUM(E."U_frete_negociado") * -1 AS "Valor"
    FROM ORIN N
        INNER JOIN RIN3 E ON E."DocEntry" = N."DocEntry" AND E."ExpnsCode" = 1
    WHERE N."CANCELED" = 'N'
        AND N."U_venda_futura" = :idContrato
        AND E."U_frete_negociado" > 0

    UNION ALL

    SELECT SUM(E."LineTotal") * -1 AS "Valor"
    FROM ORIN N
        INNER JOIN RIN3 E ON E."DocEntry" = N."DocEntry" AND E."ExpnsCode" = 1
    WHERE N."CANCELED" = 'N'
        AND N."U_venda_futura" = :idContrato
        AND (E."U_frete_negociado" IS NULL OR E."U_frete_negociado" <= 0)

    UNION ALL

    SELECT 0 AS "Valor" FROM DUMMY
) X
