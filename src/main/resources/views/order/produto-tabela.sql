SELECT
	"OITM"."ItemCode" AS "IDPRODUTOERP",
	"OITM"."U_grupo_sustennutri" AS "IDGRUPOPRODUTOERP",
	"OITM"."ItemName" AS "DESCRICAO",
	"ITM1"."PriceList"  AS "IDTABPRECOERP",
	"ITM1"."Price" AS "VALOR1",
	c."U_desconto" AS "DESCMAXIMO1",
	"OITM"."SalUnitMsr" AS "UNDMEDIDUNDMEDIDA",
	(SELECT "FirmName"  FROM OMRC WHERE "FirmCode" = -1) AS "MARCA",
	"OITM"."UserText" AS "OBSERVACAOGERAL",
	"OITM"."U_categoria" AS "IDCATEGORIAERP",
	"OITM"."U_linha_sustennutri" AS "IDLINHAPRODUTOERP"
FROM
	"OITM"
	INNER JOIN "ITM1" on("ITM1"."ItemCode" = "OITM"."ItemCode")
	INNER JOIN OPLN p on(p."ListNum" = "ITM1"."PriceList")
	INNER JOIN "@COMISSAO" c on(c."Code" = p."U_tipoComissao")
WHERE
    (
        "OITM"."ItemCode" like :search
        OR "OITM"."ItemName" like :search

    )
    AND "ITM1"."Price" > :zero
	AND "ITM1"."PriceList" IN(SELECT
                              	"OPLN"."ListNum"
                              FROM
                              	"OPLN"
                              	INNER JOIN "@LIBERAPARA" on("@LIBERAPARA"."Code" = "OPLN"."U_tipoComissao")
                              where
                                  "@LIBERAPARA"."U_Filial" in(
                                                SELECT
                                                    rfv."U_filial" AS "IDEMPRESAERP"
                                                FROM
                                                    OSLP o
                                                    INNER JOIN "@RO_FILIAL_LINHA" rfv ON o."U_filial" = rfv."DocEntry"
                                                WHERE
                                                    o."U_Integracao_sovis" = 1
                                                    AND o."SlpCode" = :vendedor
                                                )
                                  OR "@LIBERAPARA"."U_vendedor" = :vendedor)
	AND	"OITM"."validFor" = :yes