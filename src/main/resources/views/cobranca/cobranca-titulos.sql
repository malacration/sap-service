SELECT
    NS."DocEntry", NS."DocNum", NS."Serial", NS."Series",
    NS."BPLId", NS."BPLName", NS."CardCode", NS."CardName",
    NS."DocDate", NS."DocTotal",
    V."SlpCode", V."SlpName",
    P."InstlmntID", P."InsTotal", P."PaidToDate", P."DueDate", P."Status" AS "StatusParcela",
    C."U_Status", C."U_Cobrador", C."U_Acao", C."U_Situacao",
    C."U_Ocorrencia", C."U_Observacao", C."U_DataAcao", C."U_DataPromessa"
FROM OINV NS
    INNER JOIN INV6 P ON P."DocEntry" = NS."DocEntry"
    LEFT JOIN OSLP V ON V."SlpCode" = NS."SlpCode"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'NF' AND C."U_DocEntry" = NS."DocEntry" AND C."U_InstlmntID" = P."InstlmntID"
WHERE
    NS."CANCELED" = 'N'
    AND P."InsTotal" <> 0
    AND (
         (P."Status" = 'O'
          AND P."DueDate" <= :data)
      OR C."Code" IS NOT NULL
    )
    AND (P."Status"    = :statusParcela OR P."Status" < :statusParcelaIsFilter)
    AND P."DueDate" >= :vencimentoDe
    AND P."DueDate" <= :vencimentoAte
    AND (C."U_Status"    = :status   OR NS."DocEntry" < :statusIsFilter)
    AND (C."U_Cobrador"  = :cobrador OR NS."DocEntry" < :cobradorIsFilter)
    AND (C."U_Situacao"  = :situacao OR NS."DocEntry" < :situacaoIsFilter)
    AND (NS."BPLId"    = :filial   OR NS."BPLId"    < :filialIsFilter)
    AND (NS."SlpCode"  = :vendedor OR NS."SlpCode"  < :vendedorIsFilter)
    AND (NS."CardCode" = :cliente  OR NS."CardCode" < :clienteIsFilter)
ORDER BY P."DueDate", NS."DocNum"
