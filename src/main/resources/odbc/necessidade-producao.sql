-- Necessidade de producao por produto: estoque, pedido em aberto e o que esta vendido sem
-- previsao de entrega. Substitui a aba DADOS da planilha PEDIDOS COMERCIAL.
--
-- Todas as quantidades na unidade de ESTOQUE (OnHand, OpenInvQty, InvQty): pedido e nota guardam
-- a quantidade na unidade de venda, e somar as duas misturaria saca com quilo nos itens cuja
-- unidade de venda difere da de estoque.
--
-- Binds:
--   :itemDe, :itemAte  faixa de ItemCode
--   :depositos         depositos escolhidos, ja sem os excluidos por configuracao - so o ESTOQUE
--   :filiais           filiais escolhidas - recortam a venda: pedido, contrato de venda futura e
--                      nota de simples faturamento, cada um pela propria filial
--   :utilizacoes       utilizacoes de simples faturamento
--
-- Os dois recortes sao independentes: da para comparar a venda de uma filial com o estoque da
-- fabrica mais o da filial.
WITH
-- Venda futura: o contratado de cada contrato aberto, por item. O contrato guarda a quantidade
-- na unidade de venda; NumInSale converte para a de estoque.
CONTRATADO AS (
    SELECT
        linha."DocEntry"                              AS "DocEntry",
        linha."U_itemCode"                            AS "ItemCode",
        SUM(linha."U_quantity" * item."NumInSale")    AS "Qtd"
    FROM "@AR_CF_LINHA" linha
    INNER JOIN "@AR_CONTRATO_FUTURO" contrato ON contrato."DocEntry" = linha."DocEntry"
    INNER JOIN OITM item ON item."ItemCode" = linha."U_itemCode"
    WHERE
        contrato."U_status" = 'aberto'
        AND contrato."U_filial" IN (:filiais)
        AND linha."U_itemCode" BETWEEN :itemDe AND :itemAte
    GROUP BY linha."DocEntry", linha."U_itemCode"
),
-- Mesma conta do Contrato.tudoEntregue: nota de entrega menos devolucao. A apropriacao do
-- adiantamento tambem e OINV do contrato, mas sem U_entrega_vf; a devolucao de adiantamento e
-- ORIN do contrato com o item de adiantamento, que nao casa com item de contrato nenhum.
ENTREGUE AS (
    SELECT nota."U_venda_futura" AS "DocEntry", linha."ItemCode" AS "ItemCode", SUM(linha."InvQty") AS "Qtd"
    FROM OINV nota
    INNER JOIN INV1 linha ON linha."DocEntry" = nota."DocEntry"
    WHERE
        nota."CANCELED" = 'N'
        AND nota."U_entrega_vf" = 1
        AND nota."U_venda_futura" IS NOT NULL
    GROUP BY nota."U_venda_futura", linha."ItemCode"
),
DEVOLVIDO AS (
    SELECT devolucao."U_venda_futura" AS "DocEntry", linha."ItemCode" AS "ItemCode", SUM(linha."InvQty") AS "Qtd"
    FROM ORIN devolucao
    INNER JOIN RIN1 linha ON linha."DocEntry" = devolucao."DocEntry"
    WHERE
        devolucao."CANCELED" = 'N'
        AND devolucao."U_venda_futura" IS NOT NULL
        AND linha."BaseType" <> 203
    GROUP BY devolucao."U_venda_futura", linha."ItemCode"
),
-- Retirada ja pedida: tem previsao, e ja conta na coluna Pedido.
EM_PEDIDO AS (
    SELECT pedido."U_venda_futura" AS "DocEntry", linha."ItemCode" AS "ItemCode", SUM(linha."OpenInvQty") AS "Qtd"
    FROM ORDR pedido
    INNER JOIN RDR1 linha ON linha."DocEntry" = pedido."DocEntry"
    WHERE
        pedido."CANCELED" = 'N'
        AND linha."LineStatus" = 'O'
        AND pedido."U_venda_futura" IS NOT NULL
    GROUP BY pedido."U_venda_futura", linha."ItemCode"
),
-- Saldo de cada contrato limitado a zero: contrato entregue a mais nao desconta o saldo dos outros.
CONTRATO_VF AS (
    SELECT
        contratado."ItemCode" AS "ItemCode",
        SUM(GREATEST(
            contratado."Qtd" - IFNULL(entregue."Qtd", 0) + IFNULL(devolvido."Qtd", 0) - IFNULL(em_pedido."Qtd", 0),
            0)) AS "Qtd"
    FROM CONTRATADO contratado
    LEFT JOIN ENTREGUE entregue
        ON entregue."DocEntry" = contratado."DocEntry" AND entregue."ItemCode" = contratado."ItemCode"
    LEFT JOIN DEVOLVIDO devolvido
        ON devolvido."DocEntry" = contratado."DocEntry" AND devolvido."ItemCode" = contratado."ItemCode"
    LEFT JOIN EM_PEDIDO em_pedido
        ON em_pedido."DocEntry" = contratado."DocEntry" AND em_pedido."ItemCode" = contratado."ItemCode"
    GROUP BY contratado."ItemCode"
),
ESTOQUE AS (
    SELECT estoque."ItemCode" AS "ItemCode", SUM(estoque."OnHand") AS "Qtd"
    FROM OITW estoque
    WHERE
        estoque."WhsCode" IN (:depositos)
        AND estoque."ItemCode" BETWEEN :itemDe AND :itemAte
    GROUP BY estoque."ItemCode"
),
-- Pedido em aberto, menos simples faturamento: o pedido de simples faturamento vira nota sem
-- saida de mercadoria, e o que falta entregar dele entra em SIMPLES. Usage nulo e pedido normal.
PEDIDO AS (
    SELECT linha."ItemCode" AS "ItemCode", SUM(linha."OpenInvQty") AS "Qtd"
    FROM ORDR pedido
    INNER JOIN RDR1 linha ON linha."DocEntry" = pedido."DocEntry"
    WHERE
        pedido."CANCELED" = 'N'
        AND linha."LineStatus" = 'O'
        AND pedido."BPLId" IN (:filiais)
        AND IFNULL(linha."Usage", -1) NOT IN (:utilizacoes)
        AND linha."ItemCode" BETWEEN :itemDe AND :itemAte
    GROUP BY linha."ItemCode"
),
-- Simples faturamento: a nota (utilizacao de simples faturamento) fatura tudo, e cada remessa e
-- uma entrega copiada dela. O OpenInvQty da linha da nota e o que falta entregar.
SIMPLES AS (
    SELECT linha."ItemCode" AS "ItemCode", SUM(linha."OpenInvQty") AS "Qtd"
    FROM OINV nota
    INNER JOIN INV1 linha ON linha."DocEntry" = nota."DocEntry"
    WHERE
        nota."CANCELED" = 'N'
        AND linha."LineStatus" = 'O'
        AND nota."BPLId" IN (:filiais)
        AND linha."Usage" IN (:utilizacoes)
        AND linha."ItemCode" BETWEEN :itemDe AND :itemAte
    GROUP BY linha."ItemCode"
)
-- INNER JOIN em ESTOQUE: o produto precisa estar cadastrado em algum deposito escolhido.
SELECT
    item."ItemCode"          AS "ItemCode",
    item."ItemName"          AS "ItemName",
    grupo."ItmsGrpNam"       AS "Grupo",
    item."U_linha_sustennutri" AS "Linha",
    valor_linha."Descr"      AS "LinhaNome",
    valor_linha."IndexID"    AS "LinhaOrdem",
    item."InvntryUom"        AS "Unidade",
    IFNULL(estoque."Qtd", 0) AS "Estoque",
    IFNULL(pedido."Qtd", 0)  AS "Pedido",
    IFNULL(vf."Qtd", 0)      AS "ContratoVendaFutura",
    IFNULL(simples."Qtd", 0) AS "SimplesFaturamento"
FROM OITM item
INNER JOIN OITB grupo ON grupo."ItmsGrpCod" = item."ItmsGrpCod"
-- Linha de produto (OX PREMIUM, OX MEGA...): o campo guarda o codigo; o nome e a ordem vem dos
-- valores validos do campo de usuario.
LEFT JOIN CUFD campo_linha ON campo_linha."TableID" = 'OITM' AND campo_linha."AliasID" = 'linha_sustennutri'
LEFT JOIN UFD1 valor_linha
    ON valor_linha."TableID" = campo_linha."TableID"
    AND valor_linha."FieldID" = campo_linha."FieldID"
    AND valor_linha."FldValue" = item."U_linha_sustennutri"
INNER JOIN ESTOQUE estoque ON estoque."ItemCode" = item."ItemCode"
LEFT JOIN PEDIDO pedido ON pedido."ItemCode" = item."ItemCode"
LEFT JOIN CONTRATO_VF vf ON vf."ItemCode" = item."ItemCode"
LEFT JOIN SIMPLES simples ON simples."ItemCode" = item."ItemCode"
WHERE
    item."validFor" = 'Y'
    AND item."frozenFor" = 'N'
    AND item."InvntItem" = 'Y'
ORDER BY
    IFNULL(estoque."Qtd", 0) - IFNULL(pedido."Qtd", 0),
    item."ItemCode"
