-- Contratos de venda futura entregues/concluidos que receberam devolucao ou cancelamento de nota
-- depois da ultima alteracao do contrato - candidatos a voltar para aberto.
--
-- So aponta o candidato: quem decide e o ConcluiContratosSchedule, com Contrato.statusRegredido
-- (a mesma conta de entregas que concluiu o contrato). A devolucao de adiantamento da troca tambem
-- e ORIN do contrato e aparece aqui, mas nao mexe nas entregas e o contrato fica como esta.
--
-- Por que exigir o evento em vez de comparar contratado x entregue de todos: ha contratos antigos
-- entregues/concluidos com entrega que nao fecha por fluxos anteriores (474, 369, 177, 159 em
-- 16/10/2026) e eles reabririam todos no deploy. So devolucao/cancelamento novo reabre.
--
-- "UpdateDate" e so data: o documento do mesmo dia da ultima alteracao conta (>=). :desde limita a
-- janela para o candidato que nao regride nao ser reavaliado para sempre.
SELECT
    vf."DocEntry" AS "DocEntry"
FROM "@AR_CONTRATO_FUTURO" vf
WHERE
    vf."U_status" IN ('entregue', 'concluido')
    AND (
        EXISTS (
            SELECT 1
            FROM ORIN devolucao
            WHERE
                devolucao."U_venda_futura" = vf."DocEntry"
                AND devolucao."CANCELED" = 'N'
                AND devolucao."CreateDate" >= vf."UpdateDate"
                AND devolucao."CreateDate" >= :desde
        )
        OR EXISTS (
            SELECT 1
            FROM OINV nota
            WHERE
                nota."U_venda_futura" = vf."DocEntry"
                AND nota."CANCELED" = 'Y'
                AND nota."UpdateDate" >= vf."UpdateDate"
                AND nota."UpdateDate" >= :desde
        )
    )
