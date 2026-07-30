package br.andrew.sap.controllers.logistica
import br.andrew.sap.model.sap.cadastro.Regiao
import br.andrew.sap.services.logistica.RegiaoService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("regioes")
class RegiaoController(val service : RegiaoService) {

    @GetMapping()
    fun get(@RequestParam(name = "search", required = false) search : String?, page : Pageable): Page<Regiao> {
        return service.getPage(search, page)
    }

    @GetMapping("todas")
    fun getTodas(@RequestParam(name = "search", required = false) search : String?): List<Regiao> {
        return service.getTodas(search)
    }

    @GetMapping("{code}")
    fun getById(@PathVariable code : String): Regiao {
        return service.getRegiao(code)
    }

    @PostMapping()
    fun save(@RequestBody regiao : Regiao): Regiao {
        return service.criar(regiao)
    }

    @PatchMapping("{code}")
    fun update(@PathVariable code : String, @RequestBody regiao : Regiao): Regiao {
        //envia o objeto completo (com linhas/faixas ja carregadas) - essas
        //colecoes agora sao sempre serializadas (mesmo vazias), entao um
        //patch parcial vindo do front apagaria as linhas/faixas existentes
        val atual = service.getRegiao(code)
        atual.Name = regiao.Name
        atual.U_NomeRegiao = regiao.U_NomeRegiao
        atual.U_CodCordenador = regiao.U_CodCordenador
        service.update(atual, "'$code'")
        return service.getRegiao(code)
    }

    @PutMapping("{code}/filial")
    fun atualizaFilial(@PathVariable code : String, @RequestParam(required = false) filial : Int?): Regiao {
        return service.atualizaFilial(code, filial)
    }

    @PostMapping("{code}/localidades/{codLocal}")
    fun addLocalidade(@PathVariable code : String, @PathVariable codLocal : String,
                      @RequestParam(required = false) distancia : Double?): Regiao {
        return service.addLocalidade(code, codLocal, distancia)
    }

    @DeleteMapping("{code}/localidades/{codLocal}")
    fun removeLocalidade(@PathVariable code : String, @PathVariable codLocal : String): Regiao {
        return service.removeLocalidade(code, codLocal)
    }

    @PutMapping("{code}/localidades/{codLocal}/distancia")
    fun atualizaDistancia(@PathVariable code : String, @PathVariable codLocal : String,
                          @RequestParam distancia : Double): Regiao {
        return service.atualizaDistancia(code, codLocal, distancia)
    }

    @GetMapping("localidade/{codLocal}")
    fun getByLocalidade(@PathVariable codLocal : String): Regiao? {
        return service.getRegiaoByLocalidade(codLocal)
    }

    @PostMapping("{code}/faixas")
    fun addFaixa(@PathVariable code : String,
                @RequestParam qtdeMinima : Int,
                @RequestParam valorKm : Double): Regiao {
        return service.addFaixa(code, qtdeMinima, valorKm)
    }

    @PutMapping("{code}/faixas/{lineId}")
    fun atualizaFaixa(@PathVariable code : String, @PathVariable lineId : Int,
                      @RequestParam qtdeMinima : Int,
                      @RequestParam valorKm : Double): Regiao {
        return service.atualizaFaixa(code, lineId, qtdeMinima, valorKm)
    }

    @DeleteMapping("{code}/faixas/{lineId}")
    fun removeFaixa(@PathVariable code : String, @PathVariable lineId : Int): Regiao {
        return service.removeFaixa(code, lineId)
    }

    //TESTE REGIAO2 (diagnostico, remover junto com o resto)
    @DeleteMapping("{code}/localidades/{codLocal}/teste-explicito")
    fun removeLocalidadeTesteExplicito(@PathVariable code : String, @PathVariable codLocal : String): String {
        return service.removeLocalidadeTesteExplicito(code, codLocal)
    }
}
