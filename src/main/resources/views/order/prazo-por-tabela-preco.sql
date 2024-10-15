SELECT
	"OCTG"."GroupNum",
	"OCTG"."PymntGroup",
	c."Code",
	"OPLN"."ListNum",
    cond."U_desconto",
    cond."U_juros"
FROM
	"OPLN"
	INNER JOIN OPLN tt ON tt."ListNum" =  "OPLN"."ListNum"
	INNER JOIN "@COMISSAO" c ON tt."U_tipoComissao" = c."Code"
	INNER JOIN "@CONDICOESFV" cond ON cond."Code" = c."Code"
	INNER JOIN "OCTG" ON cond."U_prazo" = "OCTG"."GroupNum"
WHERE
	"OPLN"."ListNum" = :tabelaPreco