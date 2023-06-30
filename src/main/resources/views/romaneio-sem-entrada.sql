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
	rom."u_NumeroNota",
	rom."u_TaxaMoeda",
	rom."Creator",
	rom."U_ObsRomaneio",
	rom."U_PlacaCaminhao",
	rom."U_CodVeiculo",
	'all' as "todos"
FROM
	"@AMFS_RETR" rom
	LEFT JOIN "@PECU_REGR" ent ON ent."U_NumeroTicket" = rom."DocNum" AND ent."Canceled" = 'N'
WHERE
	ent."U_NumeroTicket" is NULL
	AND rom."U_CodParceiro" IN (SELECT DISTINCT "@PECU_RCIS"."U_CodParceiroNegocio" FROM "@PECU_RCIS" WHERE "Canceled" = 'N')
	AND rom."U_Status" = 'E'
	AND (rom."U_CodParceiro" = :bp OR not exists(SELECT tmp."U_CodParceiro" from "@AMFS_RETR" tmp where tmp."U_CodParceiro" = :bp))
ORDER BY rom."DocNum" desc
