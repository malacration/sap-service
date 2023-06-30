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
	rom."U_CodVeiculo"
FROM
	"@AMFS_RETR" rom
	LEFT JOIN "@PECU_REGR" ent ON ent."U_NumeroTicket" = rom."DocNum" AND ent."Canceled" = 'N'
WHERE
	ent."U_NumeroTicket" is NULL
	AND rom."U_CodParceiro" IN (SELECT DISTINCT "@PECU_RCIS"."U_CodParceiroNegocio" FROM "@PECU_RCIS" WHERE "Canceled" = 'N')
	AND rom."U_Status" = 'E'
	AND (rom."U_CodParceiro" = :bp OR not exists(SELECT tmp."U_CodParceiro" from "@AMFS_RETR" tmp
	                                                where tmp."U_CodParceiro" = :bp AND tmp."U_Status" = 'E'
	                                                AND tmp."U_CodParceiro" IN (SELECT DISTINCT "@PECU_RCIS"."U_CodParceiroNegocio" FROM "@PECU_RCIS" WHERE "Canceled" = 'N')))
	AND (rom."u_NumeroNota" = :nfNum OR not exists(SELECT tmp."u_NumeroNota" from "@AMFS_RETR" tmp where tmp."u_NumeroNota" = :nfNum AND tmp."U_Status" = 'E'))
ORDER BY rom."DocNum" desc
