 SELECT
   "OBPL"."BPLId",
   "OBPL"."BPLName",
   "OBPL"."PrefState"
 FROM OHEM o
 INNER JOIN HEM10 h  ON o."empID" = h."empID"
 INNER JOIN "OBPL" ON "OBPL"."BPLId"  = h."BPLId"
 WHERE  o."empID" = :colaborador