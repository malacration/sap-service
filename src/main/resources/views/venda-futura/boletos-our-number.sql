SELECT DISTINCT
    "IV_IB_BillOfExchange"."OurNumber"
FROM
    "IV_IB_BillOfExchange"
    INNER JOIN "ODPI" ON (
        "ODPI"."DocEntry" = "IV_IB_BillOfExchange"."DocEntry"
        AND "IV_IB_BillOfExchange"."DocType" = 203
    )
WHERE
    "ODPI"."U_venda_futura" = :idVendaFutura
    AND "IV_IB_BillOfExchange"."OurNumber" IS NOT NULL
ORDER BY
    "IV_IB_BillOfExchange"."OurNumber"
