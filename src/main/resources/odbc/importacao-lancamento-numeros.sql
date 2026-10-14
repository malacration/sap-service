-- "Numero" (OJDT.Number, o que a tela do SAP mostra) dos lancamentos recem-criados, quando a
-- resposta do $batch nao trouxe o campo: a transacao (TransId = JdtNum) sempre vem.
SELECT "TransId", "Number" AS "Numero"
FROM "OJDT"
WHERE "TransId" IN (:transacoes)
