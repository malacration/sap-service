-- Regras de distribuicao citadas no arquivo (grupo economico = dimensao 1, centro de custo =
-- dimensao 2). E a OOCR, e nao a OPRC, que o lancamento referencia: regra inativa aqui e o que
-- faz o Service Layer responder "-5002 Inactive distribution rule".
SELECT "OcrCode", "DimCode", "Active"
FROM "OOCR"
WHERE "OcrCode" IN (:codigos)
