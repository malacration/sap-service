-- Pedidos para a ordem de carregamento (substitui a antiga view pedido-search.sql), pelo
-- sap-odbc porque o SQLQueries do Service Layer nao aceita IFNULL(...) LIKE: com ele,
-- localidade e cidade em branco ("%") trazem tambem pedido sem localidade/cidade.
-- Paginado por pedido (DENSE_RANK por DocNum+DocEntry: series diferentes repetem DocNum), nao por linha: um pedido nao fica partido
-- entre paginas. "TotalDocumentos" diz se ha mais pagina.
-- Cidade sem acento e sem caixa: o Kotlin manda o termo ja em maiusculas e sem acento.
SELECT * FROM (
    SELECT P.*, MAX(P."Documento") OVER () AS "TotalDocumentos"
    FROM (
        SELECT DISTINCT
            c."DocEntry",
            c."DocNum",
            c."CardCode",
            c."CardName",
            c."DocDate",
            c."DocTotal",
            d."ItemCode",
            d."Dscription",
            d."Quantity",
            r."U_LocalidadeS",
            r."CityS",
            c."BPLId" AS "BPL_IDAssignedToInvoice",
            l."Name",
            w."OnHand",
            d."UomCode",
            d."Weight1",
            grupo."BaseQty" AS "KgConversao",
            w."IsCommited",
            w."OnOrder",
            b."DflWhs",
            x."U_Status",
            d."PriceBefDi" AS "UnitPrice",
            d."WhsCode" AS "WarehouseCode",
            d."Usage",
            d."TaxCode",
            d."OcrCode" AS "CostingCode",
            d."OcrCode2" AS "CostingCode2",
            d."LineNum" AS "BaseLine",
            d."U_preco_negociado" AS "PrecoNegociado",
            d."U_preco_base" AS "PrecoBase",
            c."Comments" AS "Comentario",
            d."DistribSum" AS "FretePorLinha",
            c."AtcEntry" AS "AttachmentEntry",
            d."DistribSum",
            e."SlpName",
            e."Telephone",
            e."Mobil",
            DENSE_RANK() OVER (ORDER BY c."DocNum", c."DocEntry") AS "Documento"
        FROM "ORDR" c
        JOIN "RDR1" d ON c."DocEntry" = d."DocEntry"
        JOIN "OITM" o ON d."ItemCode" = o."ItemCode"
        JOIN "OITW" w ON o."ItemCode" = w."ItemCode" AND d."WhsCode" = w."WhsCode"
        JOIN "OBPL" b ON c."BPLId" = b."BPLId"
        LEFT JOIN "RDR12" r ON c."DocEntry" = r."DocEntry"
        LEFT JOIN "@RO_LOCAIS" l ON r."U_LocalidadeS" = l."Code"
        LEFT JOIN "UGP1" grupo ON grupo."UgpEntry" = 4 AND grupo."UomEntry" = d."UomEntry"
        LEFT JOIN "@ORD_CRG_LINHA" m ON d."DocEntry" = m."U_orderDocEntry"
        LEFT JOIN "@ORD_CARREGAMENTO" x ON m."DocEntry" = x."DocEntry"
        LEFT JOIN "OSLP" e ON e."SlpCode" = c."SlpCode"
        WHERE c."DocDate" >= :startDate
            AND c."DocDate" <= :finalDate
            AND IFNULL(r."U_LocalidadeS", '') LIKE :localidade
            AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                    UPPER(IFNULL(r."CityS", '')),
                    'Á', 'A'), 'À', 'A'), 'Â', 'A'), 'Ã', 'A'), 'É', 'E'), 'Ê', 'E'), 'Í', 'I'),
                    'Ó', 'O'), 'Ô', 'O'), 'Õ', 'O'), 'Ú', 'U'), 'Ü', 'U'), 'Ç', 'C') LIKE :cidade
            AND c."BPLId" = :filial
            AND (c."SlpCode" = :vendedor OR c."SlpCode" > :vendedorDisable)
            AND c."DocStatus" = 'O'
            AND (x."U_Status" = 'Cancelado' OR x."U_Status" IS NULL)
            AND d."U_ORD_CARREGAMENTO" IS NULL
            AND d."Usage" = 9
    ) P
) T
WHERE T."Documento" > :inicio AND T."Documento" <= :fim
ORDER BY T."DocNum", T."DocEntry", T."BaseLine"
