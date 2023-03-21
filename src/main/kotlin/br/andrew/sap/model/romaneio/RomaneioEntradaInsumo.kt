package br.andrew.sap.model.romaneio

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class RomaneioEntradaInsumo(
        val U_ChaveNotaCO: Any?,
        val U_ModeloNotaCO: Any?,
        val U_SerieNotaCO: Any?,
        val U_NumeroNotaCO: Any?,
        val U_ObservacoesCO: String?,
        val U_CodDocumentoCO: Any?,
        val U_PesoNotaCO: Int?,
        val U_ValorNotaCO: Int,
        val U_DtEmissaoNotaCO: Any?,
        val U_CodParceiroNegocios: String?,
        val U_Moeda: String?,
        val U_TaxaCambio: Int,
        val U_PesagemAvulsa: String?,
        val U_CodTipoEmbalagem: Any?,
        val U_QtdEmbalagens: Any?,
        val U_CodFazenda: String?,
        val U_DscFazenda: String?,
        val U_CodSafra: String,
        val U_DscSafra: String,
        val U_DataEntrada: Date,
        val U_CodResponsavel: String,
        val U_CodRegistroCompra: String?,
        val U_NumeroBoletim: String?,
        val U_NomeResponsavel: String,
        val U_NumeroTicket: Int?,
        val U_CodMotorista: String?,
        val U_Motorista: String?,
        val U_PlacaCaminhao: Any?,
        val U_CodViagem: Any?,
        val U_KmInicial: Int,
        val U_KmFinal: Int,
        val U_KmTotal: Int,
        val U_CodTransportador: String?,
        val U_NomeTransportador: String?,
        val U_NotaFiscal: String,
        val U_RecebMercadoria: String,
        val U_CodDeposito: String?,
        val U_PesoNota: Int?,
        val U_PesoBruto: Int,
        val U_PesoTara: Int,
        val U_PesoLiquido: Int,
        val U_PesoLiquidoDesc: Double,
        val U_Diferenca: Double?,
        val U_EncerradoPor: String?,
        val U_DtEncerramento: String?,
        val U_HrEncerramento: String?,
        val U_Status: String?,
        val U_NumeroLaudo: Any?,
        val U_Observacoes: String?,
        val U_DevNotaSaida: Any?,
        val U_TipoRomaneio: String,
        val U_CodDocumento: String?,
        val U_NFPropria: String?,
        val U_NumeroNota: Int?,
        val U_ModeloNota: Any?,
        val U_SerieNota: Any?,
        val U_ChaveNota: Any?,
        val U_DtEmissaoNota: Any?,
        val U_SequenciaNota: String?,
        val U_DtPesagem: Any?,
        val U_HoraPesagem: Any?,
        val U_DtPesagemTara: Any?,
        val U_HoraPesagemTara: Any?,
        val U_CodRomaneioOrigem: Any?,
        val U_CodGrupoClassificacao: Any?,
        val U_CTeViagem: Any?,
        val U_PesagemManual: Any?,
        val U_DescricaoBalanca: Any?
//        val PECU_REGACollection: List<REGACollection>?,
//        val PECU_REGNCollection: List<REGNCollection>?
){
    var DocNum: Int? = null
    var Period: Int? = null
    val Instance: Int? = null
    var Series: Int? = null
    var Handwrtten: String? = null
    var Status: String? = null
    var RequestStatus: String? = null
    var Creator: String? = null
    var Remark: Any? = null
    var DocEntry: Int? = null
    var Canceled: String? = null
    var Object: String? = null
    var LogInst: Any? = null
    var UserSign: Int? = null
    var Transfered: String? = null
    var CreateDate: String? = null
    var CreateTime: String? = null
    var UpdateDate: Date? = null
    var UpdateTime: String? = null
    val DataSource: String? = null
}