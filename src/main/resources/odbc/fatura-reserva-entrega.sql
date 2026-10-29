-- O que a entrega (ODLN) de uma retirada precisa da fatura de reserva (simples faturamento,
-- OINV isIns = 'Y') para passar na TransactionNotification:
--   ChaveAcesso -> U_ChaveAcesso da entrega ("Venda Futura precisa referenciar a chave de acesso").
--     Vem da "Process" da TaxPlus, que o SQLQueries do Service Layer nao enxerga (tabela fora
--     do SAP), por isso esta consulta passa pelo sap-odbc.
--   Desonerado -> o desconto de cabecalho da entrega tem que ser igual ao ICMS desonerado dela
--     (staType 28/10 na DLN4). O da entrega e proporcional a quantidade (conferido em entregas
--     feitas no SAP), entao a linha da fatura informa o total e o servico faz a regra de tres.
--   CFOPCode -> 5922/6922 da fatura vira 5116/6116 na entrega ("CFOP Errado!").
--   LineTotal -> base do percentual de desconto (total da linha antes do desconto de cabecalho).
--   Frete* -> despesa adicional da NF (INV3; na venda futura e a 5 "Frete Simples Fatura", nao a 1
--     "Frete" do pedido). A entrega leva a mesma despesa, proporcional a quantidade entregue sobre a
--     quantidade total da NF, como as entregas feitas no SAP. So a primeira despesa da NF: nenhuma
--     NF de venda futura tem mais de uma (conferido em 2026-10-29).
SELECT
    o."CardCode",
    o."BPLId",
    o."SlpCode",
    o."Comments" AS "Observacao",
    o."DocNum" AS "NumeroNF",
    o."DiscSum" AS "DescontoNF",
    p."KeyNfe" AS "ChaveAcesso",
    i."LineNum",
    i."ItemCode",
    i."Quantity",
    i."OpenQty",
    i."LineTotal",
    i."CFOPCode",
    i."WhsCode",
    IFNULL((SELECT SUM(t."U_TX_VlDeL") FROM "INV4" t
            WHERE t."DocEntry" = i."DocEntry" AND t."LineNum" = i."LineNum" AND t."staType" IN (28, 10)), 0) AS "Desonerado",
    f."LineNum" AS "FreteLinha",
    f."ExpnsCode" AS "FreteCodigo",
    f."LineTotal" AS "Frete",
    f."DrawnTotal" AS "FreteBaixado",
    (SELECT COUNT(*) FROM "INV3" y WHERE y."DocEntry" = o."DocEntry") AS "QtdDespesas",
    f."OcrCode" AS "FreteRegra",
    f."OcrCode2" AS "FreteRegra2"
FROM "OINV" o
INNER JOIN "INV1" i ON i."DocEntry" = o."DocEntry"
LEFT JOIN "Process" p ON p."DocEntry" = o."DocEntry" AND p."DocType" = 13
LEFT JOIN "INV3" f ON f."DocEntry" = o."DocEntry"
    AND f."LineNum" = (SELECT MIN(x."LineNum") FROM "INV3" x WHERE x."DocEntry" = o."DocEntry")
WHERE o."DocEntry" = :fatura
    AND o."isIns" = 'Y'
    AND o."CANCELED" = 'N'
