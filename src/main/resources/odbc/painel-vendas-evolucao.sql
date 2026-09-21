-- Evolucao temporal do faturamento, com a serie do ANO ANTERIOR sobreposta.
--
-- Duas faixas de data na MESMA consulta (nao duas chamadas): o painel ja tem
-- risco de fan-out sobre OINV/ORIN, e duplicar a varredura por causa do
-- comparativo agravaria isso.
-- PERIODOS permite que um documento contribua para ambas as series quando
-- o intervalo cobre mais de um ano. Deslocar a data ANTES de agrupar alinha
-- tambem as semanas, sem inventar uma data 29/02 em ano nao bissexto.
--
-- A granularidade NAO e bind parameter - nome de funcao/coluna nao pode ser
-- vinculado. O backend substitui __TRUNC__ por um fragmento de whitelist
-- fechada (PainelVendasV2Service.fragmentoGranularidade). Nada vindo do front
-- entra aqui como texto.
--
-- Para SEMANA o fragmento devolve a DATA DE INICIO da semana, nunca
-- YEAR()+WEEK(): a semana que atravessa a virada do ano quebraria em duas.
--
-- Mesma regra de cancelamento dos KPIs: 'C' inverte o sinal e usa a data do
-- proprio cancelamento, para que periodo passado nao mude de valor.
WITH PERIODOS AS (
    SELECT 'ATUAL' AS SERIE, CAST(:dataInicio AS DATE) AS INICIO, CAST(:dataFim AS DATE) AS FIM FROM DUMMY
    UNION ALL
    SELECT 'ANTERIOR', CAST(:dataInicioAnterior AS DATE), CAST(:dataFimAnterior AS DATE) FROM DUMMY
)
SELECT
    SERIE,
    __TRUNC__ AS PERIODO,
    SUM(SINAL * TOTAL) AS FATURAMENTO,
    SUM(CASE WHEN ORIGEM = 'I' THEN SINAL ELSE 0 END) AS QTD_FATURAS
FROM (
    SELECT
        CASE WHEN P.SERIE = 'ANTERIOR' THEN ADD_YEARS(F."DocDate", 1) ELSE F."DocDate" END AS DATA_DOC,
        'I' AS ORIGEM,
        CASE WHEN F."CANCELED" = 'C' THEN -1 ELSE 1 END AS SINAL,
        F."DocTotal" AS TOTAL,
        P.SERIE
    FROM OINV F
    INNER JOIN PERIODOS P ON F."DocDate" >= P.INICIO AND F."DocDate" <= P.FIM
    WHERE (:todasFiliais = 1 OR F."BPLId" IN (:filiais))
      AND (:todosVendedores = 1 OR F."SlpCode" = :vendedor)

    UNION ALL

    SELECT
        CASE WHEN P.SERIE = 'ANTERIOR' THEN ADD_YEARS(D."DocDate", 1) ELSE D."DocDate" END,
        'R',
        CASE WHEN D."CANCELED" = 'C' THEN 1 ELSE -1 END,
        D."DocTotal",
        P.SERIE
    FROM ORIN D
    INNER JOIN PERIODOS P ON D."DocDate" >= P.INICIO AND D."DocDate" <= P.FIM
    WHERE (:todasFiliais = 1 OR D."BPLId" IN (:filiais))
      AND (:todosVendedores = 1 OR D."SlpCode" = :vendedor)
) MOV
GROUP BY SERIE, __TRUNC__
ORDER BY __TRUNC__
