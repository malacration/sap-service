-- Base compartilhada do painel integrado de vendas (pedidos + NF de venda futura). O servico envia
-- esta base seguida de uma "cauda" (painel-carregamento-pedidos*.sql) que continua a lista de CTEs.
-- Os filtros e as utilizacoes vem da mesma lista da busca da ordem de carregamento (:utilizacoes).
-- :sinaisFrete  sinais de TotalExpns aceitos: 1 com frete, 0 sem frete (retirada), -1,0,1 todos. Vale so para pedido.
-- :tipo         '' (os dois), 'pedido' ou 'nf-venda-futura'.
-- :emOrdem      'todos', 'sim' (ja esta numa ordem de carregamento) ou 'nao'.
-- DocDate em YYYYMMDD: o formato que o SQLQueries devolvia e que o front le.
-- Fatura de reserva (NF de venda futura) com saldo a entregar: cada linha vira uma linha por ordem
-- Aberta que reservou parte dela (Quantity = retirada) e uma do saldo livre (OpenQty menos as reservas).
WITH RESERVAS AS (
    SELECT
        R."U_docEntryFatura" AS "DocEntry",
        R."U_baseLine" AS "LineNum",
        OC."DocEntry" AS "Ordem",
        SUM(R."U_qtdRetirada") AS "Retirada"
    FROM "@ORD_CRG_LINHA" R
    INNER JOIN "@ORD_CARREGAMENTO" OC
        ON OC."DocEntry" = R."DocEntry"
       AND OC."U_Status" = 'Aberto'
    WHERE R."U_baseType" = 13
    GROUP BY R."U_docEntryFatura", R."U_baseLine", OC."DocEntry"
),
FATURAS AS (
    SELECT
        P."DocEntry",
        TO_VARCHAR(P."DocDate", 'YYYYMMDD') AS "DocDate",
        P."CardCode",
        P."CardName",
        V."SlpCode",
        V."SlpName",
        L."LineNum",
        L."ItemCode",
        L."Dscription" AS "Description",
        L."Usage",
        L."OpenQty",
        L."WhsCode",
        IFNULL(D."OnHand", 0) AS "OnHand",
        D."MinStock" AS "EstoqueMinimo",
        EE."Incoterms",
        LOCAIS."Name",
        EE."CityS",
        EE."StateS"
    FROM OINV P
    INNER JOIN INV1 L ON P."DocEntry" = L."DocEntry"
    LEFT JOIN OSLP V ON V."SlpCode" = P."SlpCode"
    LEFT JOIN INV12 EE ON P."DocEntry" = EE."DocEntry"
    LEFT JOIN OITW D ON D."WhsCode" = L."WhsCode" AND D."ItemCode" = L."ItemCode"
    LEFT JOIN "@RO_LOCAIS" LOCAIS ON LOCAIS."Code" = EE."U_LocalidadeS"
    WHERE (:tipo = '' OR :tipo = 'nf-venda-futura')
        AND P."isIns" = 'Y'
        AND P."CANCELED" = 'N'
        AND L."OpenQty" > 0
        AND P."DocDate" >= :startDate
        AND P."DocDate" <= :finalDate
        AND P."BPLId" IN (:branch)
        AND (EE."U_LocalidadeS" LIKE :localidade OR IFNULL(EE."U_LocalidadeS", '') = '')
        AND EE."Incoterms" LIKE :incoterms
        AND L."ItemCode" LIKE :search
        AND (V."SlpCode" = :vendedor OR V."SlpCode" > :vendedorDisable)
        AND P."CardCode" LIKE :partner
),
LINHAS AS (
    -- Pedido: Tipo nulo. Utilizacoes e frete valem so para esta perna.
    SELECT
        CAST(NULL AS NVARCHAR(20)) AS "Tipo",
        P."DocEntry",
        TO_VARCHAR(P."DocDate", 'YYYYMMDD') AS "DocDate",
        P."CardCode",
        P."CardName",
        V."SlpCode",
        V."SlpName",
        L."LineNum",
        L."ItemCode",
        L."Dscription" AS "Description",
        L."Usage",
        L."DistribSum",
        L."Quantity",
        L."WhsCode",
        IFNULL(D."OnHand", 0) AS "OnHand",
        D."MinStock" AS "EstoqueMinimo",
        EE."Incoterms",
        LOCAIS."Name",
        EE."CityS",
        EE."StateS",
        TO_NVARCHAR(L."U_ORD_CARREGAMENTO") AS "EmOrdemDeCarregamento"
    FROM ORDR P
    INNER JOIN RDR1 L ON P."DocEntry" = L."DocEntry"
    LEFT JOIN OSLP V ON V."SlpCode" = P."SlpCode"
    LEFT JOIN RDR12 EE ON P."DocEntry" = EE."DocEntry"
    LEFT JOIN OITW D ON D."WhsCode" = L."WhsCode" AND D."ItemCode" = L."ItemCode"
    LEFT JOIN "@RO_LOCAIS" LOCAIS ON LOCAIS."Code" = EE."U_LocalidadeS"
    WHERE (:tipo = '' OR :tipo = 'pedido')
        AND P."DocStatus" = 'O'
        AND L."Usage" IN (:utilizacoes)
        AND P."DocDate" >= :startDate
        AND P."DocDate" <= :finalDate
        AND P."BPLId" IN (:branch)
        AND SIGN(IFNULL(P."TotalExpns", 0)) IN (:sinaisFrete)
        AND EE."U_LocalidadeS" LIKE :localidade
        AND EE."Incoterms" LIKE :incoterms
        AND L."ItemCode" LIKE :search
        AND (V."SlpCode" = :vendedor OR V."SlpCode" > :vendedorDisable)
        AND P."CardCode" LIKE :partner
    UNION ALL
    -- NF de venda futura: uma linha por ordem Aberta que reservou parte da linha da fatura.
    SELECT
        'NF venda futura', F."DocEntry", F."DocDate", F."CardCode", F."CardName", F."SlpCode", F."SlpName",
        F."LineNum", F."ItemCode", F."Description", F."Usage", CAST(NULL AS DECIMAL(19, 6)),
        R."Retirada", F."WhsCode", F."OnHand", F."EstoqueMinimo", F."Incoterms", F."Name", F."CityS", F."StateS",
        TO_NVARCHAR(R."Ordem")
    FROM FATURAS F
    INNER JOIN RESERVAS R ON R."DocEntry" = F."DocEntry" AND R."LineNum" = F."LineNum"
    UNION ALL
    -- NF de venda futura: saldo livre (OpenQty menos o reservado em ordens Abertas).
    SELECT
        'NF venda futura', F."DocEntry", F."DocDate", F."CardCode", F."CardName", F."SlpCode", F."SlpName",
        F."LineNum", F."ItemCode", F."Description", F."Usage", CAST(NULL AS DECIMAL(19, 6)),
        F."OpenQty" - IFNULL(S."Reservado", 0), F."WhsCode", F."OnHand", F."EstoqueMinimo", F."Incoterms", F."Name", F."CityS", F."StateS",
        CAST(NULL AS NVARCHAR(50))
    FROM FATURAS F
    LEFT JOIN (
        SELECT "DocEntry", "LineNum", SUM("Retirada") AS "Reservado" FROM RESERVAS GROUP BY "DocEntry", "LineNum"
    ) S ON S."DocEntry" = F."DocEntry" AND S."LineNum" = F."LineNum"
    WHERE F."OpenQty" - IFNULL(S."Reservado", 0) > 0
),
FILTRADAS AS (
    SELECT *
    FROM LINHAS
    WHERE :emOrdem = 'todos'
       OR (:emOrdem = 'sim' AND IFNULL("EmOrdemDeCarregamento", '') <> '')
       OR (:emOrdem = 'nao' AND IFNULL("EmOrdemDeCarregamento", '') = '')
)
