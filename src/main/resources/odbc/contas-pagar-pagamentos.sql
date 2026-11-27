-- Conferencia de pagamento de nota de entrada (contas a pagar, Nota Agil / SoftExpert FXP-NA).
-- Aplicacoes de pagamento efetuado (VPM2 + OVPM) em UMA parcela de uma nota fiscal de entrada:
--   VPM2.DocNum   = OVPM.DocEntry (o pagamento);
--   VPM2.DocEntry = DocEntry da nota; InvType 18 = nota fiscal de entrada (OPCH);
--   VPM2.InstId   = numero da parcela (PCH6.InstlmntID).
-- Pagamento cancelado (OVPM.Canceled = 'Y') nao conta. SumApplied esta na moeda local.
-- SumApplied e o que baixou a parcela, inclusive a retencao de categoria Pagamento descontada
-- neste pagamento (WtAppld). O que saiu para o fornecedor e SumApplied - WtAppld.
-- Agendamento (assistente de pagamento nao executado) nao gera OVPM e, portanto, nao aparece aqui.
SELECT
    PR."DocEntry" AS "DocEntry",
    PR."DocNum" AS "DocNum",
    TO_VARCHAR(PR."DocDate", 'YYYY-MM-DD') AS "DocDate",
    PG."InstId" AS "InstId",
    PG."SumApplied" AS "SumApplied",
    PG."WtAppld" AS "WtAppld"
FROM VPM2 PG
INNER JOIN OVPM PR ON PR."DocEntry" = PG."DocNum"
WHERE PG."DocEntry" = :docEntry
  AND PG."InvType" = 18
  AND PG."InstId" = :parcela
  AND PR."Canceled" = 'N'
