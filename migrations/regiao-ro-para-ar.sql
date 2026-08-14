-- Migracao dos dados de Regiao de RO_REGIAO para AR_REGIAO.
--
-- Script manual, pra rodar direto no banco (nao e um SQLQuery da aplicacao -
-- por isso fica fora de src/main/resources/views, que e escaneado e
-- registrado automaticamente no SAP a cada start da aplicacao).
--
-- Pre-requisito: o UDO "Regiao" precisa ja existir apontando pra AR_REGIAO
-- (rodar a aplicacao uma vez com fields.regiao-frete=true - ver
-- RegiaoFreteConfiguration.kt) antes de rodar este script.
--
-- Faz INSERT INTO ... SELECT direto nas tabelas fisicas: passa por cima das
-- validacoes que RegiaoService.criar()/atualizaFilial() fazem (ex.: uma
-- filial so pode estar vinculada a uma unica regiao). Como os dados vem
-- direto de RO_REGIAO, que ja deveria respeitar essas regras, o risco e
-- baixo - mas vale conferir os dados em AR_REGIAO depois de rodar.

INSERT INTO "@AR_REGIAO" ("Code", "Name", "U_NomeRegiao", "U_CodCordenador", "U_Filial")
SELECT "Code", "Name", "U_NomeRegiao", "U_CodCordenador", "U_Filial"
FROM "@RO_REGIAO";

INSERT INTO "@AR_REGIAO_LINHAS" ("Code", "LineId", "U_Locais", "U_Distancia")
SELECT "Code", "LineId", "U_Locais", "U_Distancia"
FROM "@RO_REGIAO_LINHAS";

INSERT INTO "@AR_REGIAO_FAIXA" ("Code", "LineId", "U_QtdeAte", "U_ValorKm")
SELECT "Code", "LineId", "U_QtdeAte", "U_ValorKm"
FROM "@RO_REGIAO_FAIXA";

-- Conferencia rapida: as contagens devem bater dos dois lados.
-- SELECT COUNT(*) FROM "@RO_REGIAO"; SELECT COUNT(*) FROM "@AR_REGIAO";
-- SELECT COUNT(*) FROM "@RO_REGIAO_LINHAS"; SELECT COUNT(*) FROM "@AR_REGIAO_LINHAS";
-- SELECT COUNT(*) FROM "@RO_REGIAO_FAIXA"; SELECT COUNT(*) FROM "@AR_REGIAO_FAIXA";
