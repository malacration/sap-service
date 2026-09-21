SELECT
    NS."DocEntry", NS."DocNum", NS."Serial", NS."Series",
    NS."BPLId", NS."BPLName", NS."CardCode", NS."CardName",
    CL."Phone1" AS "Telefone", CL."Cellular" AS "Celular",
    NS."DocDate", NS."DocTotal",
    V."SlpCode", V."SlpName",
    P."InstlmntID", P."InsTotal", P."PaidToDate", P."DueDate", P."Status" AS "StatusParcela",
    C."U_Status", C."U_Cobrador", C."U_Acao", C."U_Situacao",
    C."U_Ocorrencia", C."U_Observacao", C."U_DataAcao", C."U_DataPromessa",
    PR."DocDate" AS "DataPagamento", PG."SumApplied" AS "ValorPago", PR."Comments" AS "ObservacaoPagamento"
FROM OINV NS
    INNER JOIN INV6 P ON P."DocEntry" = NS."DocEntry"
    LEFT JOIN OSLP V ON V."SlpCode" = NS."SlpCode"
    LEFT JOIN OCRD CL ON CL."CardCode" = NS."CardCode"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'NF' AND C."U_DocEntry" = NS."DocEntry" AND C."U_InstlmntID" = P."InstlmntID"
    LEFT JOIN RCT2 PG
         ON PG."DocEntry" = NS."DocEntry" AND PG."InstId" = P."InstlmntID" AND PG."InvType" = 13
         AND EXISTS (
             SELECT 1 FROM ORCT PRX WHERE PRX."DocEntry" = PG."DocNum" AND (PRX."Canceled" = 'N' OR PRX."Canceled" IS NULL)
         )
    LEFT JOIN ORCT PR
         ON PR."DocEntry" = PG."DocNum"
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
    AND NS."DocDate" >= :lancamentoDe
    AND NS."DocDate" <= :lancamentoAte
    AND (C."U_Status"    = :status   OR NS."DocEntry" < :statusIsFilter)
    AND (C."U_Status"    LIKE :statusPrefixo   OR NS."DocEntry" < :statusPrefixoIsFilter)
    AND (C."U_Cobrador"  = :cobrador OR NS."DocEntry" < :cobradorIsFilter)
    AND (C."U_Cobrador"  LIKE :cobradorPrefixo OR NS."DocEntry" < :cobradorPrefixoIsFilter)
    AND (C."U_Situacao"  = :situacao OR NS."DocEntry" < :situacaoIsFilter)
    AND (C."U_Situacao"  LIKE :situacaoPrefixo OR NS."DocEntry" < :situacaoPrefixoIsFilter)
    AND (C."Code" IS NULL OR NS."DocEntry" < :semAcompanhamentoIsFilter)
    AND (
        NS."DocEntry" < :comAcompanhamentoIsFilter
        OR EXISTS (
            SELECT 1 FROM RCT2 PGX
                INNER JOIN ORCT PRX ON PRX."DocEntry" = PGX."DocNum" AND (PRX."Canceled" = 'N' OR PRX."Canceled" IS NULL)
                INNER JOIN "@COB_TITULO" CX ON CX."U_Tipo" = 'NF' AND CX."U_DocEntry" = PGX."DocEntry" AND CX."U_InstlmntID" = PGX."InstId"
                INNER JOIN "@COB_TITULO_L" HX ON HX."Code" = CX."Code" AND HX."U_Data" <= PRX."DocDate"
            WHERE PGX."DocEntry" = NS."DocEntry" AND PGX."InstId" = P."InstlmntID" AND PGX."InvType" = 13
              AND PRX."DocDate" >= :dataPagamentoDe AND PRX."DocDate" <= :dataPagamentoAte
        )
    )
    AND (C."U_DataPromessa" <= :promessaVencidaAte OR NS."DocEntry" < :promessaVencidaIsFilter)
    AND (NS."DocDate" <> P."DueDate" OR NS."DocEntry" < :ocultarAvistaIsFilter)
    AND (PR."DocDate" >= :dataPagamentoDe OR NS."DocEntry" < :dataPagamentoDeIsFilter)
    AND (PR."DocDate" <= :dataPagamentoAte OR NS."DocEntry" < :dataPagamentoAteIsFilter)
    AND (NS."BPLId"    = :filial   OR NS."BPLId"    < :filialIsFilter)
    AND (NS."SlpCode"  = :vendedor OR NS."SlpCode"  < :vendedorIsFilter)
    AND (NS."CardCode" = :cliente  OR NS."CardCode" < :clienteIsFilter)
    AND NS."CardCode" NOT IN (SELECT "DflCust" FROM OBPL WHERE "DflCust" IS NOT NULL)
    AND (
        PG."DocEntry" IS NULL
        OR NOT EXISTS (
            SELECT 1 FROM RCT2 PG2
                INNER JOIN ORCT PR2 ON PR2."DocEntry" = PG2."DocNum" AND (PR2."Canceled" = 'N' OR PR2."Canceled" IS NULL)
            WHERE PG2."DocEntry" = PG."DocEntry" AND PG2."InstId" = PG."InstId" AND PG2."InvType" = 13
              AND PR2."DocDate" >= :dataPagamentoDe
              AND PR2."DocDate" <= :dataPagamentoAte
              AND (PR2."DocDate" > PR."DocDate"
                   OR (PR2."DocDate" = PR."DocDate" AND PG2."DocNum" > PG."DocNum"))
        )
    )
ORDER BY P."DueDate", NS."DocNum"
