-- Boletos da BankPlus pelo nosso numero, para montar o lote a partir da planilha do banco. A
-- BankPlus grava os boletos no proprio banco da empresa (IV_IB_BillOfExchange): "OurNumber" e o
-- nosso numero SEM o digito verificador (17 digitos; a planilha do BB traz "...-5"). DocType 13
-- = NF (OINV), 203 = adiantamento (ODPI); InstallmentID = parcela (INV6/DPI6). "Document" e o
-- numero da NF (o Serial), so para conferir com o "Nro beneficiario" da planilha.
-- Um nosso numero pode ter mais de uma linha (boleto cancelado e reemitido): o servico prefere a
-- que nao esta cancelada.
SELECT
    B."OurNumber" AS "NossoNumero",
    B."DocType" AS "DocType",
    B."DocEntry" AS "DocEntry",
    B."InstallmentID" AS "Parcela",
    B."Document" AS "Documento",
    B."InstallmentValue" AS "Valor",
    TO_VARCHAR(B."DueDate", 'YYYY-MM-DD') AS "Vencimento",
    B."Status" AS "Status"
FROM "IV_IB_BillOfExchange" B
WHERE B."OurNumber" IN (:nossosNumeros)
