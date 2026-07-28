SELECT
    T0."DocEntry", T0."DocNum",
    VF."DocNum" AS "ContratoDocNum",
    T0."BPLId", T0."BPLName", T0."CardCode", T0."CardName",
    T0."DocDate", T0."DocTotal",
    V."SlpCode", V."SlpName",
    P."InstlmntID", P."InsTotal", P."PaidToDate", P."DueDate", P."Status" AS "StatusParcela",
    C."U_Status", C."U_Cobrador", C."U_Acao", C."U_Situacao",
    C."U_Ocorrencia", C."U_Observacao", C."U_DataAcao", C."U_DataPromessa"
FROM ODPI T0
    INNER JOIN DPI6 P ON P."DocEntry" = T0."DocEntry"
    LEFT JOIN OSLP V ON V."SlpCode" = T0."SlpCode"
    LEFT JOIN "@AR_CONTRATO_FUTURO" VF ON VF."DocEntry" = T0."U_venda_futura"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'AD' AND C."U_DocEntry" = T0."DocEntry" AND C."U_InstlmntID" = P."InstlmntID"
WHERE
    T0."CANCELED" = 'N'
    AND P."InsTotal" <> 0
    AND (
         (P."Status" = 'O'
          AND P."DueDate" <= :data)
      OR C."Code" IS NOT NULL
    )
    AND (P."Status"    = :statusParcela OR P."Status" < :statusParcelaIsFilter)
    AND P."DueDate" >= :vencimentoDe
    AND P."DueDate" <= :vencimentoAte
    AND (C."U_Status"    = :status   OR T0."DocEntry" < :statusIsFilter)
    AND (C."U_Cobrador"  = :cobrador OR T0."DocEntry" < :cobradorIsFilter)
    AND (C."U_Situacao"  = :situacao OR T0."DocEntry" < :situacaoIsFilter)
    AND (T0."BPLId"    = :filial   OR T0."BPLId"    < :filialIsFilter)
    AND (T0."SlpCode"  = :vendedor OR T0."SlpCode"  < :vendedorIsFilter)
    AND (T0."CardCode" = :cliente  OR T0."CardCode" < :clienteIsFilter)
ORDER BY P."DueDate", T0."DocNum"
