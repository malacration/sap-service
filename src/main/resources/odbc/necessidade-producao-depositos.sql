-- Depositos em que os produtos ativos da faixa estao cadastrados, com a filial de cada um.
--
-- Alimenta os filtros da tela de necessidade de producao: a faixa de produtos vem primeiro,
-- depois os depositos possiveis e, deles, as filiais. O cadastro do item cria a linha em OITW
-- para todo deposito da empresa, entao "cadastrado" aqui e ter a linha, com ou sem saldo; o
-- saldo volta junto para a tela destacar onde ha estoque.
--
-- Os depositos excluidos por configuracao (refugo, descarte) saem no Kotlin, nao aqui: a lista
-- pode ser vazia, e IN () vazio e erro de sintaxe.
SELECT
    deposito."WhsCode"      AS "WhsCode",
    deposito."WhsName"      AS "WhsName",
    IFNULL(deposito."BPLid", 0) AS "BPLId",
    filial."BPLName"        AS "BPLName",
    SUM(estoque."OnHand")   AS "Estoque"
FROM OITW estoque
INNER JOIN OWHS deposito ON deposito."WhsCode" = estoque."WhsCode"
INNER JOIN OITM item ON item."ItemCode" = estoque."ItemCode"
LEFT JOIN OBPL filial ON filial."BPLId" = deposito."BPLid"
WHERE
    estoque."ItemCode" BETWEEN :itemDe AND :itemAte
    AND deposito."Inactive" = 'N'
    AND item."validFor" = 'Y'
    AND item."frozenFor" = 'N'
    AND item."InvntItem" = 'Y'
GROUP BY
    deposito."WhsCode",
    deposito."WhsName",
    deposito."BPLid",
    filial."BPLName"
ORDER BY
    IFNULL(deposito."BPLid", 0),
    deposito."WhsCode"
