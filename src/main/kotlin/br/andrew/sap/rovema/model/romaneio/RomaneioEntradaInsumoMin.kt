package br.andrew.sap.rovema.model.romaneio

import br.andrew.sap.rovema.model.Fazenda
import br.andrew.sap.rovema.model.MotoristaPecuaria
import br.andrew.sap.rovema.model.RegistroCompraInsumo
import br.andrew.sap.rovema.model.RomaneioPesagem
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.text.SimpleDateFormat
import java.util.*

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class RomaneioEntradaInsumoMin(
        U_DataEntrada : Date?,
        val U_NumeroTicket : Int?,
        val U_PesoNota : Double?,
        val U_PesoBruto : Double?,
        val U_PesoTara : Double?,
        val U_PlacaCaminhao : String?,
        val U_NumeroBoletim : String? = "006",
){
        constructor(pesagem : RomaneioPesagem,
                    compra : RegistroCompraInsumo,
                    motorista : MotoristaPecuaria,
                    fazenda : Fazenda) : this(
                Date(),
                pesagem.docNum,
                pesagem.u_PesoNota,
                pesagem.u_PesoBruto,
                pesagem.u_PesoTara,
                pesagem.u_PlacaCarreta,
                null
                ){
                U_PesoLiquido = pesagem.u_PesoLiquido?:0.0
                U_PesoLiquidoDesc = pesagem.u_PesoLiquidoDesc?:0.0
                U_Diferenca = pesagem.u_Diferenca?:0.0
                tipoAnalise = pesagem.tipoAnalise

                U_CodFazenda = compra.U_CodParceiroNegocio
                U_DscFazenda = compra.U_NomParceiroNegocio
                U_CodRegistroCompra = compra.code.toString()
                U_CodDeposito = compra.U_CodDeposito

                U_CodMotorista = motorista.code
                U_Motorista = motorista.U_NomeCadastro
                U_CodFazenda = fazenda.Code;
                U_DscFazenda = fazenda.U_DescriComp

                U_CodTransportador = pesagem.u_CodTransportadora
                //U_NomeTransportador = pesagem.trans "FAZENDA RIO MADEIRA | FAZENDA RIO MADEIRA"
        }

        var U_DataEntrada = SimpleDateFormat("yyyy-MM-dd").format(U_DataEntrada)
        var DocEntry : Int? = null
        var U_PesoLiquido : Double = (U_PesoBruto?:0.0)-(U_PesoTara?:0.0)
        var U_PesoLiquidoDesc : Double = (U_PesoLiquido?:0.0)
        var U_Diferenca : Double = U_PesoLiquido?: 0.0-(U_PesoNota?:0.0);

        var U_CodFazenda : String? = null
        var U_DscFazenda : String? = null

        var U_CodSafra: String? = null
        var U_DscSafra : String? = null

        var U_CodResponsavel : String? = null
        var U_NomeResponsavel : String? = null

        var U_CodTransportador : String? = null //Parceiro Negocio
        var U_NomeTransportador : String? = null

        var U_CodMotorista: String? = null; //Objeto customizado
        var U_Motorista : String? = null //Objeto customizado

        var U_CodRegistroCompra: String? = null
        var U_CodParceiroNegocios: String? = null
        var U_CodDeposito: String? = null
        val U_TipoRomaneio = "G"

        @JsonProperty("PECU_REGACollection")
        var tipoAnalise : List<TipoAnalise>? = null

        fun setSafra(){
                this.U_CodSafra = "SVE";
                this.U_DscSafra = "FAZENDA SERRA VERDE"
        }

        fun setResponsavel(){
                this.U_CodResponsavel = "243";
                this.U_NomeResponsavel = "Danielle Lima"
        }

}