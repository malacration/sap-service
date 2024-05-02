package br.andrew.sap.controllers.romaneio

import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.*
import br.andrew.sap.model.romaneio.RegistroInsumo
import br.andrew.sap.model.romaneio.RomaneioFazendaInsumo
import br.andrew.sap.model.romaneio.RomaneioPesagem
import br.andrew.sap.model.telegram.TipoMensagem
import br.andrew.sap.services.*
import br.andrew.sap.services.romaneio.RomaneioEntradaInsumoService
import br.andrew.sap.services.romaneio.RomaneioPesagemService
import br.andrew.sap.services.romaneio.RomaneioSaidaInsumoService
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("romaneio-fazenda-insumo")
class RomaneioFazendaInsumoController(
        val romaneioEntradaServie: RomaneioEntradaInsumoService,
        val romaneioPesagemService: RomaneioPesagemService,
        val fazendaService: FazendaService,
        val registroCompraInsumoService: RegistroCompraInsumoService,
        val motoristaContratoService: MotoristaContratoService,
        val motoristaPecuariaService: MotoristaPecuariaService,
        val telegram : TelegramRequestService,
        val alternateCatService: AlternateCatService,
        val romaneioSaidaServie : RomaneioSaidaInsumoService,
        val restTemplate : RestTemplate) {


    @GetMapping()
    fun getRomaneio() : Any{
        return romaneioEntradaServie.get(OrderBy(mapOf("DocNum" to Order.DESC)))
    }

    @GetMapping("/draft/{idRomaneioPesagem}")
    fun draft(@PathVariable idRomaneioPesagem : Int) : RomaneioFazendaInsumo{
        val romaneioPesagem = romaneioPesagemService
                .getById(idRomaneioPesagem).tryGetValue<RomaneioPesagem>()
        val item =
            if(romaneioPesagem.u_CodItem == null)
                ""
            else if(romaneioPesagem.u_CodParceiro == null)
                romaneioPesagem.u_CodItem
            else {
                alternateCatService
                    .get(romaneioPesagem.u_CodItem, romaneioPesagem.u_CodParceiro)
                    ?.substitute
                    ?: romaneioPesagem.u_CodItem
            }

        val contrato = registroCompraInsumoService
                .getByItemAndPn(item,romaneioPesagem.u_CodParceiro?:"")
                .tryGetValues<RegistroInsumo>()
                .firstOrNull() ?: RegistroInsumo(null,null)

        val motoristaContrato = if(romaneioPesagem.u_CodMotorista == null ) null else
            motoristaContratoService.getById("'${romaneioPesagem.u_CodMotorista}'")
                    .tryGetValue<MotoristaContrato>()

        val motoristaPecuaria = if(motoristaContrato == null || motoristaContrato.U_RegistroCNH == null) null else
            motoristaPecuariaService.getByCnh(motoristaContrato.U_RegistroCNH)
                .tryGetValues<MotoristaPecuaria>()
                .firstOrNull() ?: MotoristaPecuaria(null,null,null)

        val fazenda =
                if (contrato.U_CodigoFazenda == null)
                    Fazenda(null,null)
                else
                    fazendaService
                            .getById("'${contrato.U_CodigoFazenda}'")
                            .tryGetValue<Fazenda>()

        val romaneio = RomaneioFazendaInsumo(romaneioPesagem,contrato,motoristaPecuaria,fazenda)
                .also {
                    it.setResponsavel()
                }
        return romaneio
    }

    @GetMapping("/salvar/entrada/{idRomaneioPesagem}")
    fun salvar(@PathVariable idRomaneioPesagem : Int) : Any?{
        return romaneioEntradaServie.save(draft(idRomaneioPesagem).also {it.preparaEntrada()  }).also {
            it.tryGetValue<RomaneioFazendaInsumo>().let {
                telegram.send("Romaneio de entrada de insumo ${it.DocEntry} salvo com sucesso!",TipoMensagem.geral)
            }
        }
    }

    @GetMapping("/salvar/saida/{idRomaneioPesagem}")
    fun salvarSaida(@PathVariable idRomaneioPesagem : Int) : Any?{
        return romaneioSaidaServie.save(draft(idRomaneioPesagem).also { it.preparaSaida() }).also {
            it.tryGetValue<RomaneioFazendaInsumo>().let {
                telegram.send("Romaneio de saida de insumo ${it.DocEntry} salvo com sucesso!",TipoMensagem.geral)
            }
        }
    }




}