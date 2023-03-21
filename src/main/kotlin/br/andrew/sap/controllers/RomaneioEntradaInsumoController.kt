package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.*
import br.andrew.sap.model.romaneio.RomaneioEntradaInsumoMin
import br.andrew.sap.services.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("romaneio-entrada-insumo")
class RomaneioEntradaInsumoController(
        val romaneioEntradaServie: RomaneioEntradaInsumoService,
        val romaneioPesagemService: RomaneioPesagemService,
        val fazendaService: FazendaService,
        val registroCompraInsumoService: RegistroCompraInsumoService,
        val motoristaContratoService: MotoristaContratoService,
        val motoristaPecuariaService: MotoristaPecuariaService,
        val romaneioAnaliseService: TipoAnaliseService,
        val restTemplate : RestTemplate) {


    @GetMapping()
    fun getRomaneio() : Any{
        return romaneioEntradaServie.get(OrderBy(mapOf("DocNum" to Order.DESC)))
    }

    @GetMapping("/draft/{idRomaneioPesagem}")
    fun draft(@PathVariable idRomaneioPesagem : Int) : RomaneioEntradaInsumoMin{
        val romaneioPesagem = romaneioPesagemService
                .getById(idRomaneioPesagem).tryGetValue<RomaneioPesagem>()

        val contrato = registroCompraInsumoService
                .getByPn(romaneioPesagem.u_CodParceiro!!)
                .tryGetValues<RegistroCompraInsumo>()
                .ifEmpty { listOf(RegistroCompraInsumo(null,null)) }
                .first()

        val motoristaContrato = motoristaContratoService
                .getById(romaneioPesagem.u_CodMotorista!!).tryGetValue<MotoristaContrato>()

        val motoristaPecuaria = motoristaPecuariaService.getByCnh(motoristaContrato.U_RegistroCNH!!)
                .tryGetValues<MotoristaPecuaria>()
                .ifEmpty { listOf(MotoristaPecuaria(null,null,null)) }.first()

        val fazenda =
                if (contrato.U_CodigoFazenda == null)
                    Fazenda(null,null)
                else
                    fazendaService
                            .getById(contrato.U_CodigoFazenda)
                            .tryGetValue<Fazenda>()

        val romaneio = RomaneioEntradaInsumoMin(romaneioPesagem,contrato,motoristaPecuaria,fazenda)
                .also {
                    it.setResponsavel();
                }
        return romaneio
    }

    @GetMapping("/salvar/{idRomaneioPesagem}")
    fun salvar(@PathVariable idRomaneioPesagem : Int) : Any?{
        return romaneioEntradaServie.save(draft(idRomaneioPesagem))
    }




}