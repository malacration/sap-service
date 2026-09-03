SELECT 'ENTREGA' AS "Origem", SUM(E."U_frete_negociado") AS "Valor"
FROM OINV N
    INNER JOIN INV3 E ON E."DocEntry" = N."DocEntry" AND E."ExpnsCode" = 1
WHERE N."CANCELED" = 'N'
    AND N."U_venda_futura" = :idContrato
    AND N."U_entrega_vf" = '1'
    AND E."U_frete_negociado" > 0

UNION ALL

SELECT 'ENTREGA', SUM(E."LineTotal")
FROM OINV N
    INNER JOIN INV3 E ON E."DocEntry" = N."DocEntry" AND E."ExpnsCode" = 1
WHERE N."CANCELED" = 'N'
    AND N."U_venda_futura" = :idContrato
    AND N."U_entrega_vf" = '1'
    AND (E."U_frete_negociado" IS NULL OR E."U_frete_negociado" <= 0)

UNION ALL

SELECT 'DEVOLUCAO', SUM(E."U_frete_negociado")
FROM ORIN N
    INNER JOIN RIN3 E ON E."DocEntry" = N."DocEntry" AND E."ExpnsCode" = 1
WHERE N."CANCELED" = 'N'
    AND N."U_venda_futura" = :idContrato
    AND E."U_frete_negociado" > 0

UNION ALL

SELECT 'DEVOLUCAO', SUM(E."LineTotal")
FROM ORIN N
    INNER JOIN RIN3 E ON E."DocEntry" = N."DocEntry" AND E."ExpnsCode" = 1
WHERE N."CANCELED" = 'N'
    AND N."U_venda_futura" = :idContrato
    AND (E."U_frete_negociado" IS NULL OR E."U_frete_negociado" <= 0)
