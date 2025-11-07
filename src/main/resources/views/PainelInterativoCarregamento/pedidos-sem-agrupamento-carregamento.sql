SELECT
    P."DocEntry",
    P."DocDate",
    P."CardCode",
    P."CardName",
    V."SlpCode",
    V."SlpName",
    L."ItemCode",
    L."Dscription" AS "Description",
    L."Usage",
    L."DistribSum",
    L."Quantity",
    D."OnHand",
    EE."Incoterms",
    LOCAIS."Name" ,
    ocl."U_orderDocEntry" AS "EmOrdemDeCarregamento"
FROM ORDR P
INNER JOIN RDR1 L
    ON P."DocEntry" = L."DocEntry"
LEFT JOIN OSLP V
    ON V."SlpCode" = P."SlpCode"
LEFT JOIN RDR12 EE
    ON P."DocEntry" = EE."DocEntry"
LEFT JOIN OITW D
    ON D."WhsCode" = L."WhsCode"
   AND D."ItemCode" = L."ItemCode"
LEFT JOIN "@ORD_CRG_LINHA" ocl
    ON L."DocEntry" = ocl."U_orderDocEntry"
   AND L."ItemCode" = ocl."U_itemCode"
LEFT JOIN "@ORD_CARREGAMENTO" oc
    ON ocl."DocEntry" = oc."DocEntry"
   AND oc."U_Status" = 'Aberto'
LEFT JOIN "@RO_LOCAIS" LOCAIS
    ON LOCAIS."Code" = EE."U_LocalidadeS"
WHERE
    P."DocStatus" = 'O'
    AND L."Usage" = 9
    AND P."DocDate" >= :startDate
    AND P."DocDate" <= :finalDate
    AND P."BPLId" = :branch
    AND EE."U_LocalidadeS" LIKE :localidade
    AND EE."Incoterms" LIKE :incoterms
    AND L."ItemCode" LIKE :search
    AND V."SlpCode" LIKE :salesPerson
    AND P."CardCode" LIKE :partner
ORDER BY
    P."DocDate" DESC
