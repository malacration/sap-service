SELECT "TransId", "Line_ID", "Account", "ShortName", "BPLId", "Debit", "Credit",
       "BalDueDeb", "BalDueCred", "FCDebit", "FCCredit", "BalFcDeb", "BalFcCred"
FROM "JDT1" WHERE "TransId" IN (:ids)
ORDER BY "TransId", "Line_ID"
