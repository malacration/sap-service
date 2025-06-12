SELECT
	"OITM"."ItemCode",
	"OITM"."ItemName" as "ItemDescription",
	"ITM1"."PriceList",
	p."ListName",
	"ITM1"."Price" as "UnitPrice",
	c."U_desconto",
	"OITM"."SalUnitMsr",
	(SELECT "FirmName"  FROM OMRC WHERE "FirmCode" = -1) AS "MARCA",
	"OITM"."UserText",
	"OITW"."OnHand",
	"OITM"."PriceUnit"
FROM
	"OITM"
	INNER JOIN "ITM1" on("ITM1"."ItemCode" = "OITM"."ItemCode")
	INNER JOIN OPLN p on(p."ListNum" = "ITM1"."PriceList")
	INNER JOIN "@COMISSAO" c on(c."Code" = p."U_tipoComissao")
	INNER JOIN "OITW" on "OITW"."ItemCode" = "OITM"."ItemCode"
    INNER JOIN "OBPL" on "OBPL"."DflWhs" = "OITW"."WhsCode"
WHERE
    (
        "OITM"."ItemCode" like :item
        OR "OITM"."ItemName" like :item

    )
    AND "ITM1"."Price" > :zero