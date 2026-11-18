-- Parcelas (INV6) ainda em aberto das NFs da ordem: "Status" vira 'C' quando a parcela e paga (PIX,
-- baixa manual...). O Service Layer nao expoe isso na parcela; sem esta consulta uma parcela paga por
-- PIX antes do primeiro boleto receberia boleto (CarregamentoBoletoService).
SELECT I."DocEntry", I."InstlmntID"
FROM "INV6" I
WHERE I."DocEntry" IN (:docEntries)
  AND I."Status" = 'O'
