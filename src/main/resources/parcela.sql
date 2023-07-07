SELECT
	"U_QrCodePix" as value
FROM
	INV6 p
WHERE
	"DocEntry" =  {Codigo}
	AND "InstlmntID"  =   {ParcelaId}