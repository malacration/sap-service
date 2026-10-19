-- Apropriacoes de adiantamento ainda ativas de uma nota de entrega de venda futura.
--
-- Usada pelo ConciliacaoVendaFuturaSchedule antes de apropriar: a apropriacao e tudo ou nada sobre o
-- total da nota (ApropriacaoAdiantamento.get), entao qualquer apropriacao ativa significa entrega
-- ja apropriada. O schedule percorre todos os VFET, e um VFET a mais da mesma entrega (lancado por
-- duas instancias ao mesmo tempo, ou antes da idempotencia da reclassificacao) gerava uma segunda
-- apropriacao. Caso real: contrato 342, NF 72733 - apropriacoes 72736 e 72737 de R$ 19.714,37 cada.
--
-- A apropriacao aponta para a nota em "U_TX_DocEntryRef". "CANCELED" = 'N' tira a cancelada ('Y') e
-- o documento de cancelamento ('C').
SELECT
    apropriacao."DocEntry"        AS "DocEntry",
    apropriacao."DocNum"          AS "DocNum",
    SUM(adiantamento."DrawnSum")  AS "Apropriado"
FROM OINV apropriacao
INNER JOIN INV9 adiantamento ON adiantamento."DocEntry" = apropriacao."DocEntry"
WHERE
    apropriacao."U_TX_DocEntryRef" = :nota
    AND apropriacao."CANCELED" = 'N'
GROUP BY
    apropriacao."DocEntry",
    apropriacao."DocNum"
