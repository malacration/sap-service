-- Limite de credito cadastrado e exposicao do cliente (tudo em aberto, vencido ou a vencer),
-- numa linha so.
--
-- OCRD."Balance" e o saldo de conta que o proprio SAP mantem e usa na verificacao de credito
-- nativa. Conferido em HMG: bate com SUM(JDT1."BalDueDeb" - "BalDueCred") do parceiro. Por
-- ser saldo em aberto por linha, ja trata pagamento parcial e reconciliacao cancelada, e
-- desconta credito do cliente em aberto (pagamento a conta).
--
-- Nao filtrar por vencimento: o objetivo da regra e pegar o cliente pontual que ja esta no
-- teto do limite, e esse cliente so tem titulo a vencer. Atraso fica com ClienteEmAtrasoRegra.
--
-- Sempre UMA linha por CardCode existente, mesmo com saldo zero: a regra distingue assim
-- "cliente sem divida" (linha com zero) de "CardCode que nao existe" (nenhuma linha).
SELECT
    c."CardCode"                AS "CardCode",
    c."CreditLine"              AS "LimiteCredito",
    COALESCE(c."Balance", 0)    AS "DividaAberta"
FROM OCRD c
WHERE
    c."CardCode" = :cardCode
