-- Formas de RECEBIMENTO (OPYM Type 'I') da tela de lote. :somenteAtivas = 1 para a troca de
-- forma (todas as ativas, nao so as do parceiro: a que faltar no cadastro dele e vinculada na
-- troca); 0 para o filtro da lista, que precisa achar documento com forma ja inativada.
SELECT "PayMethCod" AS "Codigo", "Descript" AS "Descricao", "Active" AS "Ativa"
FROM OPYM
WHERE "Type" = 'I' AND (:somenteAtivas = 0 OR "Active" = 'Y')
ORDER BY "Active" DESC, "Descript"
