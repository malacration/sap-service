SELECT
	"OITM"."ItemCode",
	"OITM"."ItemName" as "ItemDescription",
	"OITM"."SalUnitMsr",
	(SELECT "FirmName"  FROM OMRC WHERE "FirmCode" = -1) AS "MARCA",
	"OITM"."UserText",
	"OITM"."PriceUnit"
FROM
	"OITM"
WHERE
    (
        "OITM"."ItemCode" like :item
        OR "OITM"."ItemName" like :item

    )
AND "OITM"."ItemCode" LIKE 'PAC%'