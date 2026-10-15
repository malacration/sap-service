-- Contas de debito e credito citadas no arquivo de importacao de lancamentos.
--
-- Postable = 'N' e conta titulo (sintetica): o SAP nao aceita lancamento nela.
-- LocManTran = 'Y' e conta associada (controle de parceiro): lancamento manual sem parceiro e
-- recusado. E o mesmo criterio que views/contas-receber.sql usa para achar as contas de cliente.
-- FrozenFor = 'Y' congela a conta, no intervalo FrozenFrom..FrozenTo quando ele existe.
SELECT "AcctCode", "AcctName", "Postable", "LocManTran", "FrozenFor", "FrozenFrom", "FrozenTo"
FROM "OACT"
WHERE "AcctCode" IN (:contas)
