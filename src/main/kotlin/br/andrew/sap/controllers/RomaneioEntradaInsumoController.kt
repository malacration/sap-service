package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.*
import br.andrew.sap.model.romaneio.RomaneioEntradaInsumoMin
import br.andrew.sap.model.telegram.TipoMensagem
import br.andrew.sap.services.*
import br.andrew.sap.services.romaneio.RomaneioEntradaInsumoService
import br.andrew.sap.services.romaneio.RomaneioPesagemService
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import javax.xml.catalog.Catalog

@RestController
@RequestMapping("romaneio-entrada-insumo")
class RomaneioEntradaInsumoController(
        val romaneioEntradaServie: RomaneioEntradaInsumoService,
        val romaneioPesagemService: RomaneioPesagemService,
        val fazendaService: FazendaService,
        val registroCompraInsumoService: RegistroCompraInsumoService,
        val motoristaContratoService: MotoristaContratoService,
        val motoristaPecuariaService: MotoristaPecuariaService,
        val telegram : TelegramRequestService,
        val alternateCatService: AlternateCatService,
        val restTemplate : RestTemplate) {


    @GetMapping()
    fun getRomaneio() : Any{
        return romaneioEntradaServie.get(OrderBy(mapOf("DocNum" to Order.DESC)))
    }

    @GetMapping("/draft/{idRomaneioPesagem}")
    fun draft(@PathVariable idRomaneioPesagem : Int) : RomaneioEntradaInsumoMin{
        val romaneioPesagem = romaneioPesagemService
                .getById(idRomaneioPesagem).tryGetValue<RomaneioPesagem>()
        val item =
            if(romaneioPesagem.u_CodItem == null)
                ""
            else if(romaneioPesagem.u_CodParceiro == null)
                romaneioPesagem.u_CodItem!!
            else {
                alternateCatService
                    .get(romaneioPesagem.u_CodItem!!, romaneioPesagem.u_CodParceiro!!)
                    ?.substitute
                    ?: romaneioPesagem.u_CodItem
            }

        val contrato = registroCompraInsumoService
                .getByItemAndPn(item,romaneioPesagem.u_CodParceiro?:"")
                .tryGetValues<RegistroCompraInsumo>()
                .firstOrNull() ?: RegistroCompraInsumo(null,null)

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

        val romaneio = RomaneioEntradaInsumoMin(romaneioPesagem,contrato,motoristaPecuaria,fazenda)
                .also {
                    it.setResponsavel()
                }
        return romaneio
    }

    @GetMapping("/salvar/{idRomaneioPesagem}")
    fun salvar(@PathVariable idRomaneioPesagem : Int) : Any?{
        return romaneioEntradaServie.save(draft(idRomaneioPesagem)).also {
            it.tryGetValue<RomaneioEntradaInsumoMin>().let {
                telegram.send("Romaneio de entrada de insumo ${it.DocEntry} salvo com sucesso!",TipoMensagem.geral)
            }
        }
    }




}