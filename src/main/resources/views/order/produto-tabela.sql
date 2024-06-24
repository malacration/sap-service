SELECT
	"OITM"."ItemCode",
	"OITM"."U_grupo_sustennutri",
	"OITM"."ItemName" as "ItemDescription",
	"ITM1"."PriceList",
	p."ListName",
	"ITM1"."Price" as "UnitPrice",
	c."U_desconto",
	"OITM"."SalUnitMsr",
	(SELECT "FirmName"  FROM OMRC WHERE "FirmCode" = -1) AS "MARCA",
	"OITM"."UserText",
	"OITM"."U_categoria",
	"OITM"."U_linha_sustennutri"
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
    AND "OPLN"."U_publica_forca" = 1
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