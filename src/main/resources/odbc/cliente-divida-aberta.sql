-- Limite de credito cadastrado e divida vencida em aberto do cliente, numa linha so.
--
-- Mesmo par JDT1/ITR1/OITR de views/cliente-em-atraso.sql (titulo em aberto = lancamento
-- contabil do parceiro sem reconciliacao), agora agregado com SUM. A agregacao e o motivo
-- desta consulta passar pelo sap-odbc: o SQLQueries do Service Layer recusa GROUP BY
-- (ver o comentario de PainelVendasV2Service).
--
-- CURRENT_DATE direto aqui. Pelo Service Layer nao daria: ele nao aceita NOW()/ADD_DAYS e
-- obriga a calcular a data em Kotlin (ver BusinessPartnersService.temTituloVencido).
--
-- LEFT JOIN de proposito: cliente sem nenhum titulo vencido ainda devolve UMA linha, com
-- DividaAberta = 0. A regra distingue assim "cliente sem divida" (linha com zero) de
-- "CardCode que nao existe" (nenhuma linha).
--
-- OJDT nao entra: cliente-em-atraso.sql so a usa de passagem para chegar em JDT1/ITR1, e
-- JDT1."TransId" ja e o mesmo TransId do cabecalho.
SELECT
    c."CardCode"                               AS "CardCode",
    c."CreditLine"                             AS "LimiteCredito",
    COALESCE(SUM(tl."Debit" - tl."Credit"), 0) AS "DividaAberta"
FROM OCRD c
LEFT JOIN JDT1 tl ON tl."ShortName" = c."CardCode" AND tl."DueDate" <= CURRENT_DATE
LEFT JOIN ITR1 rl ON rl."TransId" = tl."TransId"
LEFT JOIN OITR r ON r."ReconNum" = rl."ReconNum"
WHERE
    c."CardCode" = :cardCode
    AND r."ReconNum" IS NULL
GROUP BY c."CardCode", c."CreditLine"
