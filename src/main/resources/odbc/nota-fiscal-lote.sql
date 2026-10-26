-- Documentos da tela de lote, em duas pernas com as mesmas colunas:
--   NF = NF-e de saida (OINV, Model 39 = modelo 55) emitida para a SEFAZ;
--   AD = adiantamento de venda futura (ODPI com U_venda_futura = DocEntry do contrato).
-- Usada pela listagem paginada e pelo download/troca de forma de pagamento do lote.
-- "Categoria" separa a NF de venda futura do PADRAO ANTIGO (simples faturamento, CFOP
-- 5922/6922 - fatura de reserva, isIns = 'Y', com as entregas feitas em cima dela) da NF de
-- saida comum: NF, NF_FUTURA ou AD. Em fatura de reserva com entrega o SAP nao deixa trocar o
-- PeyMethod ("Field cannot be updated"; conferido no HOMOLOG2 em 2026-10-06).
-- OINV e ODPI tem DocEntry proprio: o mesmo numero pode existir nas duas, por isso a chave
-- de um documento e Tipo + DocEntry, e o download manda as duas listas separadas.
--
-- NF: "emitida para a SEFAZ" = tem linha na "Process" da TaxPlus COM chave de acesso. Nota sem
-- essa linha nunca foi transmitida e nao tem DANFE nem XML. CANCELED = 'C' e o documento de
-- cancelamento que o SAP gera, nao uma nota: fica de fora; a cancelada de verdade (CANCELED =
-- 'Y') entra, com status CANCELADO - o contador tambem precisa do XML dela.
--
-- AD: adiantamento nao emite NF-e (Model 0, sem linha na "Process" - conferido no HOMOLOG2 em
-- 2026-10-06, 2.587 de 2.587 em 2026), entao so tem boleto. Fica de fora o "reconhecimento de
-- adiantamento de devolucao" que o estorno de venda futura cria com o mesmo U_venda_futura: ele
-- tem U_TX_DocEntryRef (a devolucao de origem) e nao e cobranca. U_venda_futura e NUMERICO no
-- banco (o FieldMd do codigo diz alfanumerico, mas a coluna nao e): IFNULL(..., '') nele da
-- "invalid number" no HANA, por isso IFNULL(..., 0) > 0. Status como em
-- VW_OP_CONTRATO_VF_EVENTOS: CANCELADO, DEVOLVIDO (nota de credito ativa com BaseType 203 -
-- adiantamento nao tem /Cancel, o estorno e por devolucao), FECHADO (pago) ou ABERTO.
--
-- Filtros condicionais seguem o painel de vendas: flag/sentinela desliga o filtro, porque
-- o sap-odbc exige que todo parametro enviado apareca na consulta e `IN ()` e erro de
-- sintaxe. :filiais, :docEntries e :adiantamentos nunca vao vazias; :todasFiliais / :todosDocs
-- desligam. Datas sempre vao preenchidas (o servico usa uma faixa larga quando a tela nao
-- manda): comparar DATE com '' e erro de conversao no HANA, entao vencimento usa flag.
-- No download (:todosDocs = 0) cada perna so traz o que esta na sua lista.
--
-- VENCIMENTO e por PARCELA (INV6 / DPI6), nao pelo DocDueDate: o DocDueDate e sempre o
-- vencimento da ULTIMA parcela (conferido no HOMOLOG2 em 2026-10-01: 9.094 de 9.094 notas de
-- 2026).
--
-- :porParcela = 1 (listagem da tela): uma linha por parcela, com valor (InsTotal, o valor
-- original da parcela) e vencimento dela; o filtro de vencimento vale por parcela. O documento
-- continua sendo a unidade do lote: a tela junta as linhas pela chave. :porParcela = 0
-- (download, troca e o "adicionar todas do filtro"): so a primeira parcela, uma linha por
-- documento, e o documento entra se QUALQUER parcela vence no intervalo.
--
-- :numero compara o "Serial" da NF e o DocNum do adiantamento (que nao tem Serial). A
-- numeracao da NF e por serie/filial: sem filtro de filial vem a de todas as filiais.
-- :statusSefaz so existe na NF; com ele ligado o adiantamento sai.
--
-- Paginacao por ROW_NUMBER em vez de LIMIT/OFFSET com bind; COUNT(*) OVER () traz o total
-- do filtro na mesma ida ao banco. :tipo desliga a perna inteira (nao varre a outra tabela).
-- O filtro de status SAP fica em NUMERADAS porque o status e um CASE calculado nas pernas -
-- filtrar depois do ROW_NUMBER quebraria a paginacao. :formaPagamento (PeyMethod) tambem fica
-- la, valendo para as duas pernas: '' = todas, '-' = documento sem forma.
--
-- BOLETO da BankPlus por parcela: a BankPlus grava os boletos no banco da empresa
-- (IV_IB_BillOfExchange; DocType 13 = NF, 203 = adiantamento; InstallmentID = parcela). Boleto
-- cancelado e reemitido deixa mais de uma linha por parcela: vale a mais recente (maior ID), por
-- isso o CTE BOLETOS com ROW_NUMBER. Status = IV_IB_BoeStatus (Confirmado, Baixado, Cancelado...),
-- Situation = IV_IB_BillOfExchangeSituation (Normal, Liquidado...). :boletoStatus: -2 = todos,
-- -1 = sem boleto, senao o ID do status; por parcela vale para a linha, por documento o documento
-- entra se ALGUMA parcela atende vencimento E status ao mesmo tempo (um EXISTS so: em EXISTS
-- separados, uma parcela no periodo e outra no status poriam no lote documento que a lista nao mostra).
WITH BOLETOS AS (
    SELECT
        B."DocType", B."DocEntry", B."InstallmentID", B."OurNumber", B."Status", B."Situation",
        ROW_NUMBER() OVER (PARTITION BY B."DocType", B."DocEntry", B."InstallmentID" ORDER BY B."ID" DESC) AS "Ordem"
    FROM "IV_IB_BillOfExchange" B
),
NOTAS AS (
    SELECT
        'NF' AS "Tipo",
        CASE WHEN EXISTS (
            SELECT 1 FROM INV1 CF
            WHERE CF."DocEntry" = N."DocEntry" AND CF."CFOPCode" IN ('5922', '6922')) THEN 'NF_FUTURA'
            ELSE 'NF'
        END AS "Categoria",
        N."DocEntry" AS "DocEntry",
        N."DocNum" AS "DocNum",
        N."Serial" AS "Numero",
        N."SeriesStr" AS "Serie",
        CAST(NULL AS INTEGER) AS "Contrato",
        CAST(NULL AS INTEGER) AS "ContratoDocEntry",
        TO_VARCHAR(N."DocDate", 'YYYY-MM-DD') AS "DocDate",
        N."CardCode" AS "CardCode",
        N."CardName" AS "CardName",
        N."BPLId" AS "BPLId",
        N."BPLName" AS "BPLName",
        N."DocTotal" AS "DocTotal",
        TO_VARCHAR(PARC."PrimeiroVencimento", 'YYYY-MM-DD') AS "VencimentoInicial",
        TO_VARCHAR(N."DocDueDate", 'YYYY-MM-DD') AS "VencimentoFinal",
        IFNULL(PARC."Parcelas", 0) AS "Parcelas",
        PA."InstlmntID" AS "Parcela",
        PA."InsTotal" AS "ValorParcela",
        TO_VARCHAR(PA."DueDate", 'YYYY-MM-DD') AS "VencimentoParcela",
        BO."OurNumber" AS "NossoNumero",
        BO."Status" AS "BoletoStatusId",
        BS."Description" AS "BoletoStatus",
        BSI."Description" AS "BoletoSituacao",
        N."SlpCode" AS "SlpCode",
        V."SlpName" AS "SlpName",
        N."PeyMethod" AS "FormaPagamento",
        FP."Descript" AS "FormaPagamentoNome",
        P."KeyNfe" AS "ChaveAcesso",
        P."StatusId" AS "StatusSefazId",
        S."Description" AS "StatusSefaz",
        CASE
            WHEN N."CANCELED" = 'Y' THEN 'CANCELADO'
            WHEN N."DocStatus" = 'C' THEN 'FECHADO'
            ELSE 'ABERTO'
        END AS "StatusSap"
    FROM OINV N
    INNER JOIN "Process" P ON P."DocEntry" = N."DocEntry" AND P."DocType" = N."ObjType"
    LEFT JOIN "ProcessStatus" S ON S."ID" = P."StatusId"
    LEFT JOIN OSLP V ON V."SlpCode" = N."SlpCode"
    LEFT JOIN OPYM FP ON FP."PayMethCod" = N."PeyMethod"
    LEFT JOIN (
        SELECT I."DocEntry", MIN(I."DueDate") AS "PrimeiroVencimento", COUNT(*) AS "Parcelas",
            MIN(I."InstlmntID") AS "PrimeiraParcela"
        FROM INV6 I
        GROUP BY I."DocEntry"
    ) PARC ON PARC."DocEntry" = N."DocEntry"
    LEFT JOIN INV6 PA ON PA."DocEntry" = N."DocEntry"
        AND (:porParcela = 1 OR PA."InstlmntID" = PARC."PrimeiraParcela")
    LEFT JOIN BOLETOS BO ON BO."DocType" = 13 AND BO."DocEntry" = N."DocEntry"
        AND BO."InstallmentID" = PA."InstlmntID" AND BO."Ordem" = 1
    LEFT JOIN "IV_IB_BoeStatus" BS ON BS."ID" = BO."Status"
    LEFT JOIN "IV_IB_BillOfExchangeSituation" BSI ON BSI."ID" = BO."Situation"
    WHERE (:tipo = '' OR :tipo = 'NF')
      AND N."Model" = 39
      AND N."CANCELED" <> 'C'
      AND IFNULL(P."KeyNfe", '') <> ''
      AND N."DocDate" >= :dataInicial
      AND N."DocDate" <= :dataFinal
      AND (:todasFiliais = 1 OR N."BPLId" IN (:filiais))
      AND (:todosDocs = 1 OR N."DocEntry" IN (:docEntries))
      AND (:cardCode = '' OR N."CardCode" = :cardCode)
      AND (:vendedor = -1 OR N."SlpCode" = :vendedor)
      AND (:numeroDe = -1 OR N."Serial" >= :numeroDe)
      AND (:numeroAte = -1 OR N."Serial" <= :numeroAte)
      AND (:porParcela = 0 OR :todosVencimentos = 1
           OR (PA."DueDate" >= :vencimentoDe AND PA."DueDate" <= :vencimentoAte))
      AND (:porParcela = 0 OR :boletoStatus = -2 OR IFNULL(BO."Status", -1) = :boletoStatus)
      AND (:porParcela = 1 OR (:todosVencimentos = 1 AND :boletoStatus = -2) OR EXISTS (
                SELECT 1 FROM INV6 VI
                LEFT JOIN BOLETOS VB ON VB."DocType" = 13 AND VB."DocEntry" = VI."DocEntry"
                    AND VB."InstallmentID" = VI."InstlmntID" AND VB."Ordem" = 1
                WHERE VI."DocEntry" = N."DocEntry"
                  AND (:todosVencimentos = 1 OR (VI."DueDate" >= :vencimentoDe AND VI."DueDate" <= :vencimentoAte))
                  AND (:boletoStatus = -2 OR IFNULL(VB."Status", -1) = :boletoStatus)))
      AND (:statusSefaz = -1 OR P."StatusId" = :statusSefaz)

    UNION ALL

    SELECT
        'AD' AS "Tipo",
        'AD' AS "Categoria",
        D."DocEntry" AS "DocEntry",
        D."DocNum" AS "DocNum",
        D."DocNum" AS "Numero",
        CAST(NULL AS NVARCHAR(10)) AS "Serie",
        VF."DocNum" AS "Contrato",
        D."U_venda_futura" AS "ContratoDocEntry",
        TO_VARCHAR(D."DocDate", 'YYYY-MM-DD') AS "DocDate",
        D."CardCode" AS "CardCode",
        D."CardName" AS "CardName",
        D."BPLId" AS "BPLId",
        D."BPLName" AS "BPLName",
        D."DocTotal" AS "DocTotal",
        TO_VARCHAR(PARC."PrimeiroVencimento", 'YYYY-MM-DD') AS "VencimentoInicial",
        TO_VARCHAR(D."DocDueDate", 'YYYY-MM-DD') AS "VencimentoFinal",
        IFNULL(PARC."Parcelas", 0) AS "Parcelas",
        PA."InstlmntID" AS "Parcela",
        PA."InsTotal" AS "ValorParcela",
        TO_VARCHAR(PA."DueDate", 'YYYY-MM-DD') AS "VencimentoParcela",
        BO."OurNumber" AS "NossoNumero",
        BO."Status" AS "BoletoStatusId",
        BS."Description" AS "BoletoStatus",
        BSI."Description" AS "BoletoSituacao",
        D."SlpCode" AS "SlpCode",
        V."SlpName" AS "SlpName",
        D."PeyMethod" AS "FormaPagamento",
        FP."Descript" AS "FormaPagamentoNome",
        CAST(NULL AS NVARCHAR(50)) AS "ChaveAcesso",
        CAST(NULL AS INTEGER) AS "StatusSefazId",
        CAST(NULL AS NVARCHAR(100)) AS "StatusSefaz",
        CASE
            WHEN D."CANCELED" <> 'N' THEN 'CANCELADO'
            WHEN EXISTS (
                SELECT 1 FROM RIN1 R
                INNER JOIN ORIN O ON O."DocEntry" = R."DocEntry" AND O."CANCELED" = 'N'
                WHERE R."BaseType" = 203 AND R."BaseEntry" = D."DocEntry") THEN 'DEVOLVIDO'
            WHEN D."DocStatus" = 'C' THEN 'FECHADO'
            ELSE 'ABERTO'
        END AS "StatusSap"
    FROM ODPI D
    LEFT JOIN "@AR_CONTRATO_FUTURO" VF ON VF."DocEntry" = D."U_venda_futura"
    LEFT JOIN OSLP V ON V."SlpCode" = D."SlpCode"
    LEFT JOIN OPYM FP ON FP."PayMethCod" = D."PeyMethod"
    LEFT JOIN (
        SELECT I."DocEntry", MIN(I."DueDate") AS "PrimeiroVencimento", COUNT(*) AS "Parcelas",
            MIN(I."InstlmntID") AS "PrimeiraParcela"
        FROM DPI6 I
        GROUP BY I."DocEntry"
    ) PARC ON PARC."DocEntry" = D."DocEntry"
    LEFT JOIN DPI6 PA ON PA."DocEntry" = D."DocEntry"
        AND (:porParcela = 1 OR PA."InstlmntID" = PARC."PrimeiraParcela")
    LEFT JOIN BOLETOS BO ON BO."DocType" = 203 AND BO."DocEntry" = D."DocEntry"
        AND BO."InstallmentID" = PA."InstlmntID" AND BO."Ordem" = 1
    LEFT JOIN "IV_IB_BoeStatus" BS ON BS."ID" = BO."Status"
    LEFT JOIN "IV_IB_BillOfExchangeSituation" BSI ON BSI."ID" = BO."Situation"
    WHERE (:tipo = '' OR :tipo = 'AD')
      AND IFNULL(D."U_venda_futura", 0) > 0
      AND IFNULL(D."U_TX_DocEntryRef", 0) = 0
      AND :statusSefaz = -1
      AND D."DocDate" >= :dataInicial
      AND D."DocDate" <= :dataFinal
      AND (:todasFiliais = 1 OR D."BPLId" IN (:filiais))
      AND (:todosDocs = 1 OR D."DocEntry" IN (:adiantamentos))
      AND (:cardCode = '' OR D."CardCode" = :cardCode)
      AND (:vendedor = -1 OR D."SlpCode" = :vendedor)
      AND (:numeroDe = -1 OR D."DocNum" >= :numeroDe)
      AND (:numeroAte = -1 OR D."DocNum" <= :numeroAte)
      AND (:porParcela = 0 OR :todosVencimentos = 1
           OR (PA."DueDate" >= :vencimentoDe AND PA."DueDate" <= :vencimentoAte))
      AND (:porParcela = 0 OR :boletoStatus = -2 OR IFNULL(BO."Status", -1) = :boletoStatus)
      AND (:porParcela = 1 OR (:todosVencimentos = 1 AND :boletoStatus = -2) OR EXISTS (
                SELECT 1 FROM DPI6 VI
                LEFT JOIN BOLETOS VB ON VB."DocType" = 203 AND VB."DocEntry" = VI."DocEntry"
                    AND VB."InstallmentID" = VI."InstlmntID" AND VB."Ordem" = 1
                WHERE VI."DocEntry" = D."DocEntry"
                  AND (:todosVencimentos = 1 OR (VI."DueDate" >= :vencimentoDe AND VI."DueDate" <= :vencimentoAte))
                  AND (:boletoStatus = -2 OR IFNULL(VB."Status", -1) = :boletoStatus)))
),
NUMERADAS AS (
    SELECT
        NOTAS.*,
        ROW_NUMBER() OVER (ORDER BY "DocDate" DESC, "Tipo" DESC, "DocEntry" DESC, "Parcela") AS "Linha",
        COUNT(*) OVER () AS "Total"
    FROM NOTAS
    WHERE (:statusSap = '' OR "StatusSap" = :statusSap)
      AND (:categoria = '' OR "Categoria" = :categoria)
      AND (:formaPagamento = ''
           OR (:formaPagamento = '-' AND IFNULL("FormaPagamento", '') = '')
           OR "FormaPagamento" = :formaPagamento)
)
SELECT *
FROM NUMERADAS
WHERE "Linha" > :inicio AND "Linha" <= :fim
ORDER BY "Linha"
