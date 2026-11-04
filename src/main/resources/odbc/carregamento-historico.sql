-- Historico da ordem de carregamento: cada gravacao vira uma versao no log do UDO
-- (@AORD_CARREGAMENTO, LogInst), com quem fez (U_usuario, login da tela) e o que fez (U_acao).
-- O UserSign do SAP e sempre o usuario do Service Layer, nao serve para dizer quem foi.
-- Retiradas/QtdRetirada = linhas de NF de venda futura naquela versao (@AORD_CRG_LINHA).
SELECT
    h."LogInst",
    h."UpdateDate",
    h."UpdateTime",
    h."U_Status" AS "Status",
    h."U_usuario" AS "Usuario",
    h."U_acao" AS "Acao",
    h."U_placa" AS "Placa",
    h."U_motorista" AS "Motorista",
    h."U_transportadora" AS "Transportadora",
    h."U_capacidadeCaminhao" AS "Capacidade",
    (SELECT COUNT(*) FROM "@AORD_CRG_LINHA" l WHERE l."DocEntry" = h."DocEntry" AND l."LogInst" = h."LogInst") AS "Retiradas",
    (SELECT IFNULL(SUM(l."U_qtdRetirada"), 0) FROM "@AORD_CRG_LINHA" l WHERE l."DocEntry" = h."DocEntry" AND l."LogInst" = h."LogInst") AS "QtdRetirada"
FROM "@AORD_CARREGAMENTO" h
WHERE h."DocEntry" = :ordem
ORDER BY h."LogInst"
