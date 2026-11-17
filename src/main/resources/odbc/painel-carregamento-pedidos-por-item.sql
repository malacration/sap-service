-- Painel integrado de vendas, pedidos agrupados por item.
-- Antiga view pedidos-agrupado-item-carregamento.sql do Service Layer, pelo sap-odbc para filtrar pela mesma lista de utilizacoes
-- da busca da ordem de carregamento (:utilizacoes, de carregamento.utilizacoes, padrao 9).
-- :sinaisFrete  sinais de TotalExpns aceitos: 1 com frete, 0 sem frete (retirada), -1,0,1 todos.
SELECT
L."ItemCode",
L."Dscription" AS "Description",
sum(L."Quantity") AS "Quantity",
sum(D."OnHand") AS "OnHand",
D."MinStock" as "EstoqueMinimo",
L."U_ORD_CARREGAMENTO" AS "EmOrdemDeCarregamento"
FROM ORDR P
INNER JOIN RDR1 L ON P."DocEntry" = L."DocEntry"
left JOIN OSLP V ON V."SlpCode" = P."SlpCode"
LEFT JOIN RDR12 EE ON P."DocEntry" = EE."DocEntry"
LEFT JOIN OITW D ON D."WhsCode" = L."WhsCode" AND D."ItemCode" = L."ItemCode"
LEFT JOIN "@ORD_CARREGAMENTO" oc ON L."U_ORD_CARREGAMENTO"  = oc."DocEntry" AND oc."U_Status" = 'Aberto'
WHERE P."DocStatus" = 'O'
AND L."Usage" IN (:utilizacoes)
AND P."DocDate" >= :startDate
AND P."DocDate" <= :finalDate
AND P."BPLId" = :branch
AND SIGN(IFNULL(P."TotalExpns", 0)) IN (:sinaisFrete)
AND EE."U_LocalidadeS" LIKE :localidade
AND EE."Incoterms" LIKE :incoterms
AND  (
        L."ItemCode" like :search
    )
AND (
        (V."SlpCode" = :vendedor OR V."SlpCode" > :vendedorDisable)
)
AND (
        P."CardCode" like :partner
)
GROUP BY
L."ItemCode",
L."Dscription",
 L."U_ORD_CARREGAMENTO",
D."MinStock"
ORDER BY L."ItemCode"

