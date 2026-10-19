-- Parceiros do lote que ja tem a forma de pagamento no cadastro (CRD2). Os que nao voltarem
-- aqui recebem o vinculo depois da troca: o SAP aceita PATCH de PaymentMethod no documento
-- mesmo com a forma fora do cadastro do parceiro (visto no HOMOLOG em 2026-10-06), entao
-- esperar o erro "Linked payment method" nao basta para o parceiro ficar com a forma.
SELECT DISTINCT "CardCode" AS "CardCode"
FROM CRD2
WHERE "PymCode" = :forma
  AND "CardCode" IN (:parceiros)
