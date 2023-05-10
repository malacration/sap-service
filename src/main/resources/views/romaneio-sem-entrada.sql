SELECT
	rom."DocNum",
	rom."DocEntry",
	rom."Canceled",
	rom."userSign",
	rom."U_CodParceiro",
	rom."U_NomeParceiro",
	rom."Status",
	rom."RequestStatus",
	rom."Transfered",
	rom."createDate",
	rom."createTime",
	rom."updateDate",
	rom."updateTime",
	rom."dataSource",
	rom."u_QtdRetida",
	rom."U_Status",
	rom."u_PesagemManual",
	rom."u_PesagemAvulsa",
	rom."u_TaxaMoeda",
	rom."Creator",
	rom."U_ObsRomaneio"
FROM
	"@AMFS_RETR" rom
	LEFT JOIN "@PECU_REGR" ent ON ent."U_NumeroTicket" = rom."DocNum" AND ent."Canceled" = 'N'
WHERE
	ent."U_NumeroTicket" is NULL
	AND rom."U_CodParceiro" IN (SELECT DISTINCT "@PECU_RCIS"."U_CodParceiroNegocio" FROM "@PECU_RCIS" WHERE "Canceled" = 'N')
ORDER BY rom."DocNum" desc