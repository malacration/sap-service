-- Conferencia de pagamento de nota de entrada (contas a pagar, Nota Agil / SoftExpert FXP-NA).
-- Parcelas (PCH6) de uma nota fiscal de entrada (OPCH) pelo DocEntry.
-- InsTotal e PaidToDate estao na moeda LOCAL. Por isso a linha traz tambem a moeda do documento
-- (OPCH.DocCur) e a moeda local da empresa (OADM.MainCurncy): o SoftExpert nao informa moeda e a
-- conferencia so suporta nota na moeda local - nota em outra moeda (ou moeda desconhecida) vira
-- MOEDA_NAO_SUPORTADA, nunca PAGO. OADM tem uma unica linha.
-- PaidToDate soma TUDO o que baixou a parcela (pagamento, devolucao, reconciliacao...); quem
-- prova pagamento efetuado e a consulta contas-pagar-pagamentos.sql.
-- Valor liquido (o SoftExpert manda o liquido da parcela):
--   retencao de categoria Fatura (PCH5.Category = 'I') ja sai do DocTotal e das parcelas: InsTotal
--   ja e liquido dela;
--   retencao de categoria Pagamento (PCH5.Category = 'P') so e descontada no pagamento: InsTotal a
--   inclui. "RetencaoPagamento" e o total dela no documento e "WTSum" (PCH6) a parte da parcela;
--   o sap-rovema so usa WTSum quando a soma das parcelas bate com RetencaoPagamento.
-- PCH5.AbsEntry e o DocEntry da nota.
SELECT
    P."InstlmntID" AS "InstlmntID",
    TO_VARCHAR(P."DueDate", 'YYYY-MM-DD') AS "DueDate",
    P."InsTotal" AS "InsTotal",
    P."PaidToDate" AS "PaidToDate",
    P."Status" AS "Status",
    P."WTSum" AS "WTSum",
    (SELECT SUM(W."WTAmnt") FROM PCH5 W
        WHERE W."AbsEntry" = P."DocEntry" AND W."Category" = 'P') AS "RetencaoPagamento",
    N."DocCur" AS "DocCur",
    A."MainCurncy" AS "MainCurncy"
FROM PCH6 P
INNER JOIN OPCH N ON N."DocEntry" = P."DocEntry"
CROSS JOIN OADM A
WHERE P."DocEntry" = :docEntry
