SELECT
	cond."U_prazo" AS "IDPRAZOPAGTOERP",
	OCTG."PymntGroup",
	c."Code",
	t."ListNum"
FROM
	OPLN t
	INNER JOIN OPLN tt ON tt."ListNum" =  t."ListNum"
	INNER JOIN "@COMISSAO" c ON tt."U_tipoComissao" = c."Code"
	INNER JOIN "@CONDICOESFV" cond ON cond."Code" = c."Code"
	INNER JOIN OCTG ON cond."U_prazo" = OCTG."GroupNum"
WHERE
	t."ListNum" = :tabelaPreco