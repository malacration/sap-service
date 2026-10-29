SELECT
    P."DocEntry",
    P."DocDate",
    P."CardCode",
    P."CardName",
    V."SlpCode",
    V."SlpName",
    L."LineNum" AS "BaseLine",
    L."ItemCode",
    L."Dscription" AS "Description",
    L."Usage",
    L."OpenQty",
    L."WhsCode",
    D."OnHand",
    D."MinStock" AS "EstoqueMinimo",
    EE."Incoterms",
    LOCAIS."Name",
    EE."CityS",
    EE."StateS",
    R."U_qtdRetirada" AS "Retirada",
    OC."DocEntry" AS "OrdemAberta"
FROM OINV P
INNER JOIN INV1 L ON P."DocEntry" = L."DocEntry"
LEFT JOIN OSLP V ON V."SlpCode" = P."SlpCode"
LEFT JOIN INV12 EE ON P."DocEntry" = EE."DocEntry"
LEFT JOIN OITW D ON D."WhsCode" = L."WhsCode" AND D."ItemCode" = L."ItemCode"
LEFT JOIN "@RO_LOCAIS" LOCAIS ON LOCAIS."Code" = EE."U_LocalidadeS"
LEFT JOIN "@ORD_CRG_LINHA" R ON R."U_docEntryFatura" = L."DocEntry" AND R."U_baseLine" = L."LineNum" AND R."U_baseType" = 13
LEFT JOIN "@ORD_CARREGAMENTO" OC ON OC."DocEntry" = R."DocEntry" AND OC."U_Status" = 'Aberto'
WHERE P."isIns" = 'Y'
    AND P."CANCELED" = 'N'
    AND L."OpenQty" > 0
    AND P."DocDate" >= :startDate
    AND P."DocDate" <= :finalDate
    AND P."BPLId" = :branch
    AND (EE."U_LocalidadeS" LIKE :localidade OR IFNULL(EE."U_LocalidadeS", '') = '')
    AND EE."Incoterms" LIKE :incoterms
    AND L."ItemCode" LIKE :search
    AND V."SlpCode" LIKE :salesPerson
    AND P."CardCode" LIKE :partner
ORDER BY
    P."DocDate" DESC
