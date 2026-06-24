SELECT
    vf."DocEntry"
FROM
    "@AR_CONTRATO_FUTURO" vf
WHERE
    vf."U_status" = 'aberto'
    AND EXISTS (
        SELECT
            1
        FROM
            OINV nota
        WHERE
            nota."U_venda_futura" = vf."DocEntry"
            AND nota."CANCELED" = 'N'
    )
