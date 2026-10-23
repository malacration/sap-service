-- Status de boleto da BankPlus (IV_IB_BoeStatus: Gerado, Remessa, Confirmado, Baixado, Cancelado,
-- Aguardando Cancelamento...), para o filtro "Boleto (BankPlus)" da tela de lote. Da tabela, nao de
-- lista fixa: os IDs sao da BankPlus.
SELECT "ID" AS "Id", "Description" AS "Descricao"
FROM "IV_IB_BoeStatus"
ORDER BY "ID"
