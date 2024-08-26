package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.*
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.model.sap.TableMd
import br.andrew.sap.model.sap.TbType
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.sustennutri"], havingValue = "true", matchIfMissing = false)
class DeterminacaoUtilizacao(
    val userFieldsMDService: UserFieldsMDService,
    val tableService: UserTablesMDService
) {
    init {
        tableService.findOrCreate(
            TableMd(
                "Utilizacoes", "Utilizações", TbType.bott_DocumentLines
            )
        )
        listOf(
            FieldMd("Utilizacao", "Utilizações", "@Utilizacoes", DbType.db_Alpha)
                .also {
                    it.ValidValuesMD = listOf(
                        ValuesMd("116-ADIANT. A FORNECEDOR", "ADIANT. A FORNECEDOR"),
                        ValuesMd("117-ADIANT. VIAGEM", "ADIANT. VIAGEM"),
                        ValuesMd("121-APONTAMENTO OFICINA", "APONTAMENTO OFICINA"),
                        ValuesMd("120-APONTAMENTO RURAL", "APONTAMENTO RURAL"),
                        ValuesMd("128-AQUIS DE SERV S FIN", "AQUIS DE SERV S FIN"),
                        ValuesMd("34-AQUISIÇÃO DE SERVIÇO", "AQUISIÇÃO DE SERVIÇO"),
                        ValuesMd("24-AQUISIÇÃO DE FRETE", "AQUISIÇÃO DE FRETE"),
                        ValuesMd("65-ARRENDAMENTO", "ARRENDAMENTO"),
                        ValuesMd("35-BAIXA/PERDA", "BAIXA/PERDA"),
                        ValuesMd("130-BONIFICAÇÃO/AVULSA", "BONIFICAÇÃO/AVULSA"),
                        ValuesMd("129-BONIFICAÇÃO/VENDA", "BONIFICAÇÃO/VENDA"),
                        ValuesMd("73-CARTAO CARD IDEAL", "CARTAO CARD IDEAL"),
                        ValuesMd("122-CARTAO ELO BRASIL", "CARTAO ELO BRASIL"),
                        ValuesMd("74-CARTAO VISA BRADESCO", "CARTAO VISA BRADESCO"),
                        ValuesMd("114-COM PESO EXPOR C FIN", "COM PESO EXPOR C FIN"),
                        ValuesMd("112-COM PESO EXPOR S FIN", "COM PESO EXPOR S FIN"),
                        ValuesMd("7-COMP CONSUMO C/ICMS", "COMP CONSUMO C/ICMS"),
                        ValuesMd("105-COMP CONSUMO COMBUST", "COMP CONSUMO COMBUST"),
                        ValuesMd("43-COMP CONSUMO S/ICMS", "COMP CONSUMO S/ICMS"),
                        ValuesMd("126-COMPLE ICMS INSUMO", "COMPLE ICMS INSUMO"),
                        ValuesMd("111-COMPLE PREÇO EXPORTA", "COMPLE PREÇO EXPORTA"),
                        ValuesMd("109-COMPLE PREÇO INSUMO", "COMPLE PREÇO INSUMO"),
                        ValuesMd("61-COMPLEMENTO DE PREÇO", "COMPLEMENTO DE PREÇO"),
                        ValuesMd("97-COMPLEMENTO ICMS ST", "COMPLEMENTO ICMS ST"),
                        ValuesMd("80-COMPLEMENTO PESO", "COMPLEMENTO PESO"),
                        ValuesMd("98-COMPLEP PESO SECADOR", "COMPLEP PESO SECADOR"),
                        ValuesMd("8-COMPRA ATIVO C/ICMS", "COMPRA ATIVO C/ICMS"),
                        ValuesMd("22-COMPRA ATIVO S/ICMS", "COMPRA ATIVO S/ICMS"),
                        ValuesMd("113-COMPRA FUTURA IMOB", "COMPRA FUTURA IMOB"),
                        ValuesMd("19-COMPRA FUTURA INSUMO", "COMPRA FUTURA INSUMO"),
                        ValuesMd("103-COMPRA FUTURA USO", "COMPRA FUTURA USO"),
                        ValuesMd("15-COMPRA INSUMO", "COMPRA INSUMO"),
                        ValuesMd("106-COMPRA INSUMO ST", "COMPRA INSUMO ST"),
                        ValuesMd("118-COMPRA INSUMO Á ORDE", "COMPRA INSUMO Á ORDE"),
                        ValuesMd("3-COMPRA P/ REVENDA", "COMPRA P/ REVENDA"),
                        ValuesMd("6-CONSERTO/REPARO SAID", "CONSERTO/REPARO SAID"),
                        ValuesMd("55-DEMONSTRAÇÃO ENTRADA", "DEMONSTRAÇÃO ENTRADA"),
                        ValuesMd("11-DEMONSTRAÇÃO SAIDA", "DEMONSTRAÇÃO SAIDA"),
                        ValuesMd("91-DEV COMODATO SAIDA", "DEV COMODATO SAIDA"),
                        ValuesMd("37-DEV COMPRA CONSUMO", "DEV COMPRA CONSUMO"),
                        ValuesMd("38-DEV COMPRA INDUSTRIA", "DEV COMPRA INDUSTRIA"),
                        ValuesMd("84-DEV CONSIG SAIDA", "DEV CONSIG SAIDA"),
                        ValuesMd("102-DEV TRANSF MERC TERC", "DEV TRANSF MERC TERC"),
                        ValuesMd("50-DEV TRANSF PRODUCAO", "DEV TRANSF PRODUCAO"),
                        ValuesMd("100-DEV VENDA TERCEIROS", "DEV VENDA TERCEIROS"),
                        ValuesMd("53-DEVOL CONSIG ENTRADA", "DEVOL CONSIG ENTRADA"),
                        ValuesMd("45-DEVOLUÇÃO COMODATO", "DEVOLUÇÃO COMODATO"),
                        ValuesMd("95-DEVOLUÇÃO COMPRA", "DEVOLUÇÃO COMPRA"),
                        ValuesMd("39-DEVOLUÇÃO DE VENDAS", "DEVOLUÇÃO DE VENDAS"),
                        ValuesMd("49-DEVOLUÇÃO ENTREGA", "DEVOLUÇÃO ENTREGA"),
                        ValuesMd("54-DEVOLUÇÃO SIMPLES FA", "DEVOLUÇÃO SIMPLES FA"),
                        ValuesMd("12-DOAÇÃO/BRINDE", "DOAÇÃO/BRINDE"),
                        ValuesMd("13-ENERGIA ELETRICA", "ENERGIA ELETRICA"),
                        ValuesMd("27-ENTRADA ARMAZEM", "ENTRADA ARMAZEM"),
                        ValuesMd("90-ENTRADA COMODATO", "ENTRADA COMODATO"),
                        ValuesMd("44-ENTRADA COMODATO FIN", "ENTRADA COMODATO FIN"),
                        ValuesMd("83-ENTRADA CONSIGNAÇÃO", "ENTRADA CONSIGNAÇÃO"),
                        ValuesMd("70-ENTRADA INVENTÁRIO", "ENTRADA INVENTÁRIO"),
                        ValuesMd("119-ENTRADA VENDA Á ORDE", "ENTRADA VENDA Á ORDE"),
                        ValuesMd("48-ENTREGA  MERCADORIA", "ENTREGA  MERCADORIA"),
                        ValuesMd("20-ESTORNO NFE ENTRADA", "ESTORNO NFE ENTRADA"),
                        ValuesMd("21-ESTORNO NFE SAIDA", "ESTORNO NFE SAIDA"),
                        ValuesMd("124-EXPORTAÇÃO DIRETA", "EXPORTAÇÃO DIRETA"),
                        ValuesMd("81-FAT. LOCAÇÃO", "FAT. LOCAÇÃO"),
                        ValuesMd("33-FOLHA DE PAGAMENTO", "FOLHA DE PAGAMENTO"),
                        ValuesMd("1-INDUSTRIALIZAÇÃO", "INDUSTRIALIZAÇÃO"),
                        ValuesMd("89-NF COMPLEMENTAR ICMS", "NF COMPLEMENTAR ICMS"),
                        ValuesMd("72-NOTAS BENEFICIOS", "NOTAS BENEFICIOS"),
                        ValuesMd("36-OUTRAS CONTAS A PAGA", "OUTRAS CONTAS A PAGA"),
                        ValuesMd("31-OUTRAS ENTRADAS", "OUTRAS ENTRADAS"),
                        ValuesMd("32-OUTRAS SAIDAS", "OUTRAS SAIDAS"),
                        ValuesMd("28-REMESSA ARMAZEM SAÍD", "REMESSA ARMAZEM SAÍD"),
                        ValuesMd("76-REMESSA COMODATO SAI", "REMESSA COMODATO SAI"),
                        ValuesMd("52-REMESSA CONSIG SAIDA", "REMESSA CONSIG SAIDA"),
                        ValuesMd("69-REMESSA EXPORTAÇÃO", "REMESSA EXPORTAÇÃO"),
                        ValuesMd("78-REMESSA GARANTIA SAI", "REMESSA GARANTIA SAI"),
                        ValuesMd("51-REMESSA MER FALTANTE", "REMESSA MER FALTANTE"),
                        ValuesMd("96-REMESSA MONSTRUARIO", "REMESSA MONSTRUARIO"),
                        ValuesMd("99-REMESSA P/CONSERTO", "REMESSA P/CONSERTO"),
                        ValuesMd("93-REMESSA PARA INDUSTR", "REMESSA PARA INDUSTR"),
                        ValuesMd("46-RENEGOCIACAO", "RENEGOCIACAO"),
                        ValuesMd("30-RETORNO ARMAZEM ENTR", "RETORNO ARMAZEM ENTR"),
                        ValuesMd("29-RETORNO ARMAZEM SAID", "RETORNO ARMAZEM SAID"),
                        ValuesMd("77-RETORNO COMODATO ENT", "RETORNO COMODATO ENT"),
                        ValuesMd("88-RETORNO CONSERTO", "RETORNO CONSERTO"),
                        ValuesMd("57-RETORNO DEMONST. ENT", "RETORNO DEMONST. ENT"),
                        ValuesMd("56-RETORNO DEMONSTRAÇÃO", "RETORNO DEMONSTRAÇÃO"),
                        ValuesMd("79-RETORNO GARANTIA ENT", "RETORNO GARANTIA ENT"),
                        ValuesMd("94-RETORNO INDUSTRI ENT", "RETORNO INDUSTRI ENT"),
                        ValuesMd("71-SAÍDA INVENTARIO", "SAÍDA INVENTARIO"),
                        ValuesMd("107-SIM FAT ENTRADA IMOB", "SIM FAT ENTRADA IMOB"),
                        ValuesMd("18-SIMPLES FAT ENTRADA", "SIMPLES FAT ENTRADA"),
                        ValuesMd("16-SIMPLES FAT SAIDA", "SIMPLES FAT SAIDA"),
                        ValuesMd("14-TELECOMUNICAÇÃO", "TELECOMUNICAÇÃO"),
                        ValuesMd("104-TRANSF CONS ENTRADA", "TRANSF CONS ENTRADA"),
                        ValuesMd("59-TRANSF CONS SAIDA", "TRANSF CONS SAIDA"),
                        ValuesMd("60-TRANSF DE SALDO ICMS", "TRANSF DE SALDO ICMS"),
                        ValuesMd("58-TRANSF IMOB ENTRADA", "TRANSF IMOB ENTRADA"),
                        ValuesMd("25-TRANSF IMOB SAIDA", "TRANSF IMOB SAIDA"),
                        ValuesMd("47-TRANSF MERC ENTRADA", "TRANSF MERC ENTRADA"),
                        ValuesMd("115-TRANSF MERC PROD ENT", "TRANSF MERC PROD ENT"),
                        ValuesMd("110-TRANSF MERC PROD SAI", "TRANSF MERC PROD SAI"),
                        ValuesMd("5-TRANSF MERC SAIDA", "TRANSF MERC SAIDA"),
                        ValuesMd("10-VENDA ADQ TERCEIROS", "VENDA ADQ TERCEIROS"),
                        ValuesMd("131-VENDA ARROZ", "VENDA ARROZ"),
                        ValuesMd("125-VENDA BOITEL", "VENDA BOITEL"),
                        ValuesMd("66-VENDA BOVINOS", "VENDA BOVINOS"),
                        ValuesMd("42-VENDA CONTA ORDEM", "VENDA CONTA ORDEM"),
                        ValuesMd("17-VENDA FUTURA", "VENDA FUTURA"),
                        ValuesMd("63-VENDA IMOB. 12 MESES", "VENDA IMOB. 12 MESES"),
                        ValuesMd("75-VENDA LOCAÇÃO", "VENDA LOCAÇÃO"),
                        ValuesMd("127-VENDA MILHETO", "VENDA MILHETO"),
                        ValuesMd("68-VENDA MILHO", "VENDA MILHO"),
                        ValuesMd("9-VENDA PROD PROPRIA", "VENDA PROD PROPRIA"),
                        ValuesMd("62-VENDA SEMEM", "VENDA SEMEM"),
                        ValuesMd("40-VENDA SERVIÇOS", "VENDA SERVIÇOS"),
                        ValuesMd("67-VENDA SOJA", "VENDA SOJA"),
                        ValuesMd("123-VENDA SORGO", "VENDA SORGO"),
                        ValuesMd("64-VENDA SUCATA", "VENDA SUCATA")

                    )
                },
        ).forEach { userFieldsMDService.findOrCreate(it) }

        tableService.findOrCreate(
            TableMd(
                "FiliaisBloqueio", "Filiais", TbType.bott_DocumentLines
            )
        )
        listOf(
            FieldMd("Filial", "Filiais", "@FiliaisBloqueio", DbType.db_Alpha)
                .also {
                    it.ValidValuesMD = listOf(
                        ValuesMd(
                            "2-SUSTENNUTRI NUTRICAO ANIMAL LTDA - Matriz",
                            "SUSTENNUTRI NUTRICAO ANIMAL LTDA - Matriz"
                        ),
                        ValuesMd(
                            "3-FAZENDA RIO MADEIRA S/A - FARM - CSC - Matriz",
                            "FAZENDA RIO MADEIRA S/A - FARM - CSC - Matriz"
                        ),
                        ValuesMd(
                            "4-SUSTENNUTRI NUTRICAO ANIMAL LTDA - Filial - AC",
                            "SUSTENNUTRI NUTRICAO ANIMAL LTDA - Filial - AC"
                        ),
                        ValuesMd("5-FAZENDA RIO MADEIRA S/A - FARM - RM", "FAZENDA RIO MADEIRA S/A - FARM - RM"),
                        ValuesMd("6-FAZENDA RIO MADEIRA S/A - FARM - SERRA", "FAZENDA RIO MADEIRA S/A - FARM - SERRA"),
                        ValuesMd("7-FAZENDA RIO MADEIRA S/A - FARM - JACI", "FAZENDA RIO MADEIRA S/A - FARM - JACI"),
                        ValuesMd("8-FAZENDA RIO MADEIRA S/A - FARM - JIRAU", "FAZENDA RIO MADEIRA S/A - FARM - JIRAU"),
                        ValuesMd(
                            "9-FAZENDA RIO MADEIRA S/A - FARM - JRAMOS",
                            "FAZENDA RIO MADEIRA S/A - FARM - JRAMOS"
                        ),
                        ValuesMd(
                            "10-FAZENDA RIO MADEIRA S/A - FARM - AGARÇA",
                            "FAZENDA RIO MADEIRA S/A - FARM - AGARÇA"
                        ),
                        ValuesMd(
                            "11-SUSTENNUTRI NUTRICAO ANIMAL LTDA - Filial - RO",
                            "SUSTENNUTRI NUTRICAO ANIMAL LTDA - Filial - RO"
                        ),
                        ValuesMd("12-FAZENDA RIO MADEIRA S/A - FARM - CD", "FAZENDA RIO MADEIRA S/A - FARM - CD"),
                        ValuesMd("13-FAZENDA RIO MADEIRA S/A - FARM - GARÇA", "FAZENDA RIO MADEIRA S/A - FARM - GARÇA"),
                        ValuesMd("14-FAZENDA RIO MADEIRA S/A - FARM - RBR", "FAZENDA RIO MADEIRA S/A - FARM - RBR"),
                        ValuesMd(
                            "15-FAZENDA RIO MADEIRA S/A - FARM - LABREA",
                            "FAZENDA RIO MADEIRA S/A - FARM - LABREA"
                        ),
                        ValuesMd("16-ROVEMA TRANSPORTES E LOGISTICA LTDA", "ROVEMA TRANSPORTES E LOGISTICA LTDA"),
                        ValuesMd(
                            "17-SUSTENNUTRI NUTRIÇAO ANIMAL LTDA - Filial Cacoal",
                            "SUSTENNUTRI NUTRIÇAO ANIMAL LTDA - Filial Cacoal"
                        ),
                        ValuesMd(
                            "18-SUSTENNUTRI NUTRICAO ANIMAL LTDA - Filial Roraima",
                            "SUSTENNUTRI NUTRICAO ANIMAL LTDA - Filial Roraima"
                        )


                    )
                },
        ).forEach { userFieldsMDService.findOrCreate(it) }
        tableService.findOrCreate(
            TableMd(
                "BLOQUEIOUTILIZACAO", "Determinção de Utilização", TbType.bott_Document
            )
        )
        listOf(
            FieldMd("TipoDocumento", "Tipo de documento", "@BloqueioUtilizacao", DbType.db_Alpha)
                .also {
                    it.ValidValuesMD = listOf(
                        ValuesMd("19-Dev.Nota Fiscal Entrada", "Dev.Nota Fiscal Entrada"),
                        ValuesMd("14-Dev.Nota fiscal de saída", "Dev.Nota fiscal de saída"),
                        ValuesMd("16-Devolução", "Devolução"),
                        ValuesMd("21-Devolução de mercadoria", "Devolução de mercadoria"),
                        ValuesMd("15-Entrega", "Entrega"),
                        ValuesMd("13-A-Nota Fiscal de saída", "Nota Fiscal de saída"),
                        ValuesMd("13-B-Nota Fiscal de venda fultura", "Nota Fiscal de venda fultura"),
                        ValuesMd("18-Nota de entrada", "Nota de entrada"),
                        ValuesMd("20-Recebimento de mercadoria", "Recebimento de mercadoria")
                    )
                },


            ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            FieldMd("Nome", "Nome da regra", "@BLOQUEIOUTILIZACAO", DbType.db_Alpha),
        ).forEach { userFieldsMDService.findOrCreate(it) }

    }
}