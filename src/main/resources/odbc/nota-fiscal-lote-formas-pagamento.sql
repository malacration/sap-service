-- Formas de RECEBIMENTO ativas (OPYM Type 'I'), para a troca de forma de pagamento na tela de
-- lote. Todas, nao so as do parceiro: a que faltar no cadastro dele e vinculada na troca.
SELECT "PayMethCod" AS "Codigo", "Descript" AS "Descricao"
FROM OPYM
WHERE "Type" = 'I' AND "Active" = 'Y'
ORDER BY "Descript"
