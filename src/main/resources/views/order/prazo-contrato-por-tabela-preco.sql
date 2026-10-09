SELECT
	cond."U_prazo" AS "GroupNum",
	"OCTG"."PymntGroup",
	c."Code",
	"OPLN"."ListNum",
    cond."U_desconto",
    cond."U_juros"
FROM
	"OPLN"
	INNER JOIN "@COMISSAO" c ON "OPLN"."U_tipoComissao" = c."Code"
	INNER JOIN "@CONDICOESFV" cond ON cond."Code" = c."Code"
	LEFT JOIN "OCTG" ON cond."U_prazo" = "OCTG"."GroupNum"
WHERE
	"OPLN"."ListNum" = :tabelaPreco
