package br.andrew.sap.rovema.model.romaneio

import br.andrew.sap.rovema.model.Fazenda
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class RomaneioEntradaInsumoMinimo(
        U_DataEntrada : Date?,
        val U_NumeroTicket : Int?,
        val U_CodDeposito : String?,
        val U_PesoNota : Double?,
        val U_PesoBruto : Double?,
        val U_PesoTara : Double?,
        val U_PlacaCaminhao : String,
        PECU_REGA : List<Filho> = listOf()

){
        var U_DataEntrada = SimpleDateFormat("dd/MM/yyyy").format(U_DataEntrada)
        var DocEntry : Int? = null
        val U_PesoLiquido : Double = (U_PesoBruto?:0.0)-(U_PesoTara?:0.0)
        val U_PesoLiquidoDesc : Double = (U_PesoLiquido?:0.0)
        val U_Diferenca : Double = U_PesoLiquido?: 0.0-(U_PesoNota?:0.0);

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

        fun setFazenda(fazenda : Fazenda){
                this.U_CodFazenda = fazenda.Code;
                this.U_DscFazenda = fazenda.U_DescriComp
        }

        fun setSafra(fazenda : Fazenda){
                this.U_CodSafra = "SVE";
                this.U_DscSafra = "FAZENDA SERRA VERDE"
        }

        fun setResponsavel(){
                this.U_CodResponsavel = "243";
                this.U_NomeResponsavel = "Danielle Lima"
        }

        fun setTransportadora(){
                U_CodTransportador = "FOR0000051"
                U_NomeTransportador = "FAZENDA RIO MADEIRA | FAZENDA RIO MADEIRA"
        }

        fun setMotorista(){
                U_CodMotorista ="013"
                U_Motorista = "Marcos de Souza"
        }
}


class Filho(
        val U_CodTipoAnalise : String,
        val U_DscTipoAnalise : String,
        val U_ValorEncontrado : Double,
        val U_DscUnidadeMedida : String,
        val U_DescontoPeso : Double
){
        var DocEntry: Int? = null
        var LineId: Int? = null

}