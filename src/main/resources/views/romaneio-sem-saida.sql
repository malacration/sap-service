SELECT
	rom."DocNum",
	rom."DocEntry",
	rom."Canceled",
	rom."UserSign",
	rom."U_CodParceiro",
	rom."U_NomeParceiro",
	rom."Status",
	rom."RequestStatus",
	rom."Transfered",
	rom."CreateDate",
	rom."CreateTime",
	rom."UpdateDate",
	rom."UpdateTime",
	rom."DataSource",
	rom."U_QtdRetida",
	rom."U_Status",
	rom."U_PesagemManual",
	rom."U_PesagemAvulsa",
	rom."U_NumeroNota",
	rom."U_TaxaMoeda",
	rom."Creator",
	rom."U_ObsRomaneio",
	rom."U_PlacaCaminhao",
	rom."U_CodVeiculo"
FROM
	"@AMFS_RETR" rom
	LEFT JOIN "@AGRI_RMSD" ent ON ent."U_NumeroTicket" = rom."DocNum" AND ent."Canceled" = 'N'
WHERE
	ent."U_NumeroTicket" is NULL
	AND rom."U_CodParceiro" IN (SELECT DISTINCT "@AGRI_CTVD"."U_CodParceiroNegocio" FROM "@AGRI_CTVD" WHERE "Canceled" = 'N')
	AND rom."U_Status" = 'E'
	AND (rom."U_CodParceiro" = :bp OR not exists(SELECT tmp."U_CodParceiro" from "@AMFS_RETR" tmp
	                                                where tmp."U_CodParceiro" = :bp AND tmp."U_Status" = 'E'
	                                                AND tmp."U_CodParceiro" IN (SELECT DISTINCT "@AGRI_CTVD"."U_CodParceiroNegocio" FROM "@AGRI_CTVD" WHERE "Canceled" = 'N')))
	AND (rom."u_NumeroNota" = :nfNum OR not exists(SELECT tmp."u_NumeroNota" from "@AMFS_RETR" tmp where tmp."u_NumeroNota" = :nfNum AND tmp."U_Status" = 'E'))
ORDER BY rom."DocNum" desc
