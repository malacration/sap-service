SELECT "DocEntry", "DocNum", "TransId", "U_TX_DocEntryRef", "U_venda_futura", "CardCode",
       "BPLId", "CANCELED", "DocTotal", "DpmAmnt"
FROM "OINV"
WHERE "U_TX_DocEntryRef" IN (:origens) AND "SeqCode" = :sequencia AND "CANCELED" IN ('N', 'Y')
ORDER BY "DocEntry"
