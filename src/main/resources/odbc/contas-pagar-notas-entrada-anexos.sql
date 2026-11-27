-- Arquivos PDF do anexo (ATC1) da nota de entrada :docEntry, para baixar pelo Service Layer
-- (Attachments2(AbsEntry)/$value?filename='FileName.FileExt'). O par XML + PDF e o padrao.
SELECT
    A."AbsEntry" AS "AbsEntry",
    A."Line" AS "Line",
    A."FileName" AS "FileName",
    A."FileExt" AS "FileExt"
FROM OPCH N
INNER JOIN ATC1 A ON A."AbsEntry" = N."AtcEntry"
WHERE N."DocEntry" = :docEntry
  AND N."CANCELED" = 'N'
  AND LOWER(A."FileExt") = 'pdf'
ORDER BY A."Line" DESC
