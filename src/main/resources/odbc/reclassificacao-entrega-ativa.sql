-- Reclassificacoes (VFET, ou VFEC depois de conciliada) ainda validas de uma entrega de venda futura.
--
-- Usada pelo ReclassificacaoEntregaVendaFuturaSchedule antes de lancar o VFET: se ja existe uma, a
-- entrega nao pode ser reclassificada de novo (ver DecisaoReclassificacao).
--
-- Mesma entrega = mesmo "Ref1" (DocNum da nota) e mesmo "Memo" (montado com o contrato e a nota),
-- a mesma chave da trava na SBO_SP_VALIDACAO_VENDA_FUTURA (sap-sql). So o DocNum nao basta: ele
-- se repete entre series/filiais.
--
-- Lancamento estornado nao conta, nem o proprio estorno ("StornoToTr" preenchido): a sanitizacao
-- estorna a reclassificacao errada para o schedule refaze-la.
SELECT
    lancamento."TransId"   AS "TransId",
    lancamento."TransCode" AS "TransCode"
FROM OJDT lancamento
WHERE
    lancamento."Ref1" = :referencia
    AND lancamento."Memo" = :memo
    AND lancamento."TransCode" IN ('VFET', 'VFEC')
    AND lancamento."StornoToTr" IS NULL
    AND NOT EXISTS (
        SELECT 1 FROM OJDT estorno WHERE estorno."StornoToTr" = lancamento."TransId"
    )
