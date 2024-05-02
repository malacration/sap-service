package br.andrew.sap.model.romaneio

import br.andrew.sap.model.Fazenda
import br.andrew.sap.model.MotoristaPecuaria
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.text.SimpleDateFormat
import java.util.*


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class RomaneioFazendaInsumo(
        val U_NumeroTicket : Int?,
        val U_PesoNota : Double?,
        val U_PesoBruto : Double?,
        val U_PesoTara : Double?,
        val U_PlacaCaminhao : String?,
        val U_NumeroBoletim : String? = "006",
        var U_Motorista : String? = null, //Objeto customizado
){
        constructor(pesagem : RomaneioPesagem,
                    compra : RegistroInsumo?,
                    motorista : MotoristaPecuaria?,
                    fazenda : Fazenda) : this(
                pesagem.docNum,
                pesagem.u_PesoNota,
                pesagem.u_PesoBruto,
                pesagem.u_PesoTara,
                pesagem.U_PlacaCaminhao,
                null
        ){
                U_PesoLiquido = pesagem.u_PesoLiquido?:0.0
                U_PesoLiquidoDesc = pesagem.u_PesoLiquidoDesc?:0.0
                U_Diferenca = pesagem.u_Diferenca?:0.0
                tipoAnaliseEntrada = pesagem.tipoAnalise
                tipoAnaliseSaida = pesagem.tipoAnalise
                U_CodVeiculo = pesagem.U_CodVeiculo

                if(compra != null){
                        U_CodFazenda = compra.U_CodParceiroNegocio
                        U_DscFazenda = compra.U_NomParceiroNegocio
                        U_CodRegistroCompra = compra.code.toString()
                        U_CodContratoVenda = compra.code.toString()

                        U_CodDeposito = compra.U_CodDeposito
                }
                U_CodMotorista = motorista?.code
                U_Motorista = motorista?.U_NomeCadastro
                U_CodFazenda = fazenda.Code
                U_DscFazenda = fazenda.U_DescriComp

                U_CodTransportador = pesagem.u_CodTransportadora
                //U_NomeTransportador = pesagem.trans "FAZENDA RIO MADEIRA | FAZENDA RIO MADEIRA"
        }

        private var dataEntrada :String? = null
        private var dataSaida :String? = null

        fun setU_DataEntrada(data : Date?){
                if(data != null)
                        dataEntrada = SimpleDateFormat("yyyy-MM-dd").format(data)
        }

        fun setU_DataSaida(data : Date?){
                if(data != null)
                        dataSaida = SimpleDateFormat("yyyy-MM-dd").format(data)
        }

        fun getU_DataEntrada(): String? {
                return dataEntrada
        }

        fun getU_DataSaida(): String? {
                return dataSaida
        }

        var DocEntry : Int? = null
        var U_PesoLiquido : Double = (U_PesoBruto?:0.0)-(U_PesoTara?:0.0)
        var U_PesoLiquidoDesc : Double = U_PesoLiquido
        var U_Diferenca : Double = U_PesoLiquido

        var U_CodFazenda : String? = null
        var U_DscFazenda : String? = null

        var U_CodVeiculo : String? = null

        var U_CodSafra: String? = null
        var U_DscSafra : String? = null

        var U_CodResponsavel : String? = null
        var U_NomeResponsavel : String? = null

        var U_CodTransportador : String? = null //Parceiro Negocio
        var U_NomeTransportador : String? = null

        var U_CodMotorista: String? = null //Objeto customizado
        // var U_Motorista : String? = null //Objeto customizado

        var U_CodRegistroCompra: String? = null
        var U_CodContratoVenda: String? = null
        var U_CodParceiroNegocios: String? = null

        var U_CodDeposito: String? = null
        var U_TipoRomaneio :String? = "G"

        @JsonProperty("PECU_REGACollection")
        var tipoAnaliseEntrada : List<TipoAnalise>? = null

        @JsonProperty("AGRI_RMSACollection")
        var tipoAnaliseSaida : List<TipoAnalise>? = null

        fun setResponsavel(){
                this.U_CodResponsavel = "243"
                this.U_NomeResponsavel = "Danielle Lima"
        }

        fun preparaSaida() {
                dataSaida = SimpleDateFormat("yyyy-MM-dd").format(Date())
                tipoAnaliseEntrada = null
                this.U_TipoRomaneio = null
                this.U_CodVeiculo = null
                this.U_CodRegistroCompra = null
        }

        fun preparaEntrada() {
                dataEntrada = SimpleDateFormat("yyyy-MM-dd").format(Date())
                tipoAnaliseSaida = null
                this.U_CodContratoVenda = null
        }

}