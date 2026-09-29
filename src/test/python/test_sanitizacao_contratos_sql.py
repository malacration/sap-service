"""Fixtures relacionais das consultas. SQLite em memória, SQL executado sem tradução.

Não substitui homologação no HANA. Executar com python3 -m unittest discover
-s src/test/python -p 'test_sanitizacao_contratos_sql.py'. Sem dependências externas.
"""
import pathlib
import sqlite3
import unittest

SQL = pathlib.Path(__file__).resolve().parents[2] / 'main/resources/odbc'


class SanitizacaoSqlTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(':memory:')
        self.db.row_factory = sqlite3.Row
        self.db.create_function('TO_NVARCHAR', 1, str)
        self.db.executescript('''
            CREATE TABLE OJDT (TransId INTEGER, Ref1 TEXT, TransCode TEXT, TransType INTEGER,
                StornoToTr INTEGER DEFAULT 0, RefDate TEXT DEFAULT '2026-09-24');
            CREATE TABLE OINV (DocEntry INTEGER, DocNum INTEGER, U_venda_futura INTEGER,
                BPLId INTEGER, CardCode TEXT, CardName TEXT, DocTotal NUMERIC, CANCELED TEXT,
                U_entrega_vf TEXT DEFAULT '1', U_TX_DocEntryRef INTEGER, SeqCode INTEGER,
                TransId INTEGER, DpmAmnt NUMERIC DEFAULT 0, DocDate TEXT DEFAULT '2026-09-24');
            CREATE TABLE JDT1 (TransId INTEGER, Line_ID INTEGER, Account TEXT, ShortName TEXT,
                BPLId INTEGER, Debit NUMERIC, Credit NUMERIC, BalDueDeb NUMERIC DEFAULT 0,
                BalDueCred NUMERIC DEFAULT 0, FCDebit NUMERIC DEFAULT 0, FCCredit NUMERIC DEFAULT 0,
                BalFcDeb NUMERIC DEFAULT 0, BalFcCred NUMERIC DEFAULT 0);
            CREATE TABLE ORIN (DocEntry INTEGER, DocNum INTEGER, DocTotal NUMERIC, CANCELED TEXT,
                CardCode TEXT DEFAULT 'C1', BPLId INTEGER DEFAULT 1, U_TX_DocEntryRef INTEGER,
                U_conciliar_automatico TEXT DEFAULT '0');
            CREATE TABLE INV1 (DocEntry INTEGER, LineNum INTEGER, Quantity NUMERIC,
                BaseEntry INTEGER, BaseType INTEGER);
            CREATE TABLE RIN1 (DocEntry INTEGER, BaseEntry INTEGER, BaseType INTEGER, BaseLine INTEGER, Quantity NUMERIC);
            CREATE TABLE OITR (ReconNum INTEGER, Canceled TEXT, ReconType INTEGER DEFAULT 0,
                ReconDate TEXT DEFAULT '2026-09-24');
            CREATE TABLE ITR1 (ReconNum INTEGER, TransId INTEGER, TransRowId INTEGER,
                SrcObjTyp INTEGER, SrcObjAbs INTEGER, ReconSum NUMERIC DEFAULT 100);
            INSERT INTO OJDT (TransId, Ref1, TransCode, TransType) VALUES (10, '30', 'VFEC', 30);
            INSERT INTO OINV (DocEntry, DocNum, U_venda_futura, BPLId, CardCode, CardName, DocTotal, CANCELED, TransId)
                VALUES (20, 30, 40, 1, 'C1', 'Cliente', 100, 'Y', 100);
            INSERT INTO INV1 VALUES (20, 0, 10, NULL, NULL);
            INSERT INTO JDT1 (TransId, Line_ID, Account, ShortName, BPLId, Debit, Credit)
                VALUES (10, 0, 'CLIENTES', 'C1', 1, 0, 100), (10, 1, 'CONTROLE', 'C1', 1, 100, 0);
        ''')

    def tearDown(self):
        self.db.close()

    def query(self, name='candidatos', **params):
        sql = (SQL / f'sanitizacao-contratos-{name}.sql').read_text()
        if name == 'candidatos':
            params = dict({'apos': 0, 'transId': 0}, **params)
        return [dict(row) for row in self.db.execute(sql, params)]

    def devolver(self, quantidade=10, cancelada='N'):
        self.db.execute("UPDATE OINV SET CANCELED='N'")
        self.db.execute('INSERT INTO ORIN (DocEntry,DocNum,DocTotal,CANCELED) VALUES (50,60,?,?)', (quantidade * 10, cancelada))
        self.db.execute('INSERT INTO RIN1 VALUES (50,20,13,0,?)', (quantidade,))

    def test_cancelada_e_vfec_elegivel_para_analise(self):
        self.assertEqual([10], [r['TransId'] for r in self.query()])
        self.db.execute("UPDATE OJDT SET TransCode='VFET'")
        self.assertEqual([], self.query())

    def test_origem_ativa_sem_devolucao_nao_entra(self):
        self.db.execute("UPDATE OINV SET CANCELED='N'")
        self.assertEqual([], self.query())

    def test_devolucao_parcial_e_integral(self):
        self.devolver(3)
        self.assertEqual(0, self.query()[0]['DevolucaoIntegral'])
        self.db.execute('UPDATE RIN1 SET Quantity=10')
        self.assertEqual(1, self.query()[0]['DevolucaoIntegral'])

    def test_devolucao_cancelada_nao_conta(self):
        self.devolver(cancelada='Y')
        self.assertEqual([], self.query())

    def test_quantidade_total_na_linha_errada_nao_significa_devolucao_integral(self):
        self.devolver(20)
        self.db.execute('INSERT INTO INV1 VALUES (20,1,10,NULL,NULL)')
        self.assertEqual(0, self.query()[0]['DevolucaoIntegral'])

    def test_nao_confunde_docnum_de_outra_filial(self):
        self.db.execute('''INSERT INTO OINV (DocEntry,DocNum,U_venda_futura,BPLId,CardCode,CardName,DocTotal,CANCELED)
            VALUES (21,30,41,2,'C1','Cliente',100,'Y')''')
        self.assertEqual([20], [r['DocEntry'] for r in self.query()])

    def test_nao_reestorna_lancamento_com_estorno_nativo(self):
        self.db.execute("INSERT INTO OJDT (TransId,Ref1,TransCode,TransType,StornoToTr) VALUES (11,'30','VFEC',30,10)")
        self.assertEqual([], self.query())

    def test_estorno_vfdv_e_reclassificacao_duplicada_sao_sinalizados(self):
        self.devolver()
        self.db.execute("INSERT INTO OJDT (TransId,Ref1,TransCode,TransType) VALUES (11,'60','VFDV',30)")
        self.assertEqual(1, self.query()[0]['EstornosDevolucao'])
        self.db.execute("INSERT INTO OJDT (TransId,Ref1,TransCode,TransType) VALUES (12,'30','VFEC',30)")
        self.db.execute("INSERT INTO JDT1 (TransId,Line_ID,ShortName,BPLId) VALUES (12,0,'C1',1)")
        self.assertTrue(all(r['ReclassificacoesOrigem'] == 2 for r in self.query()))

    def test_reconciliacao_por_transacao_e_linha_com_todas_contrapartidas(self):
        self.db.executescript('''INSERT INTO OITR (ReconNum,Canceled) VALUES (1,'N'),(2,'Y');
            INSERT INTO ITR1 (ReconNum,TransId,TransRowId,SrcObjTyp,SrcObjAbs)
                VALUES (1,10,1,30,10),(1,70,0,13,50),(2,10,0,30,10);''')
        rows = self.query('reconciliacoes', ids=10)
        self.assertEqual([1,1], [r['ReconNum'] for r in rows])
        self.assertEqual([10,70], [r['TransId'] for r in rows])
        self.assertEqual(1, rows[0]['TransRowId'])

    def test_apropriacao_cancelada_continua_visivel_sem_documento_cancelador(self):
        self.db.executescript('''INSERT INTO OINV (DocEntry,SeqCode,U_TX_DocEntryRef,CANCELED) VALUES
            (50,7,20,'N'),(51,7,20,'Y'),(52,7,20,'C'),(53,8,20,'N'),(54,7,21,'N');''')
        self.assertEqual([50,51], [r['DocEntry'] for r in self.query('apropriacoes', origens=20, sequencia=7)])

    def test_conferencia_exige_cancelamento_reconciliacao_e_saldo_zerado(self):
        self.db.executescript('''
            UPDATE OJDT SET RefDate='2026-01-13' WHERE TransId=10;
            INSERT INTO OJDT (TransId,Ref1,TransCode,TransType,StornoToTr,RefDate) VALUES (11,'30','VFEC',30,10,'2026-01-13');
            INSERT INTO OINV (DocEntry,CANCELED,DocDate) VALUES (50,'Y','2026-06-16'),(51,'C','2026-06-16');
            INSERT INTO INV1 VALUES (51,0,1,50,13);
            INSERT INTO OITR (ReconNum,Canceled) VALUES (9,'Y');
        ''')
        rows = self.query('verificacao', transId=10, apropriacoes=50, reconciliacoes=9)
        self.assertEqual(['ESTORNO','APROPRIACAO'], [r['Tipo'] for r in rows])
        # O Cancel nativo lança na data do original; a conferência devolve as duas.
        self.assertEqual([('2026-01-13','2026-01-13'),('2026-06-16','2026-06-16')], [(r['Data'], r['Original']) for r in rows])
        self.db.execute("UPDATE OITR SET Canceled='N'")
        self.db.execute('UPDATE JDT1 SET BalDueDeb=100 WHERE Line_ID=1')
        rows = self.query('verificacao', transId=10, apropriacoes=50, reconciliacoes=9)
        self.assertEqual(2, len([r for r in rows if r['Tipo'] == 'PENDENCIA']))

    def test_paginacao_nao_trunca_depois_de_duzentas_reclassificacoes(self):
        self.db.executemany('INSERT INTO OJDT (TransId,Ref1,TransCode,TransType) VALUES (?,\'30\',\'VFEC\',30)', ((i,) for i in range(11,212)))
        self.db.executemany('INSERT INTO JDT1 (TransId,ShortName,BPLId) VALUES (?,\'C1\',1)', ((i,) for i in range(11,212)))
        page = self.query()
        self.assertEqual(200, len(page))
        sql = (SQL / 'sanitizacao-contratos-candidatos.sql').read_text()
        page2 = self.db.execute(sql, dict(apos=page[-1]['TransId'], transId=0)).fetchall()
        self.assertEqual(2, len(page2))


if __name__ == '__main__':
    unittest.main()
