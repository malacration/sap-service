package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.entity.*
import br.andrew.sap.services.structs.UserObjectsMDService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("objects")
class ObjectsController(
    val userObjectsMDService: UserObjectsMDService
) {

    @GetMapping()
    fun getAllObject(page : Pageable) : Any{
        return userObjectsMDService.get(page)
    }

    @GetMapping("{name}")
    fun getObject(@PathVariable name : String) : Any{
        return userObjectsMDService
                .get(Filter(mutableListOf(Predicate("Code",name,Condicao.EQUAL))))
    }

    @GetMapping("/coluna/{coluna}/start/{valor}")
    fun getObjectComplexo(@PathVariable coluna : String, @PathVariable valor : String ) : Any{
        return userObjectsMDService
                .get(Filter(mutableListOf(Predicate(coluna,valor,Condicao.STARTS_WITH))))
    }

    //TODO remover depois
    @GetMapping("/create")
    fun create(){
        val ud = UserDefinedObject("comissao", "Comissões New", "COMISSAO",)
        delete(ud)
        ud.UserObjectMD_ChildTables.addAll(listOf(
            ChildTables("CONDICOESFV"),
            ChildTables("LIBERAPARA")
        ))

        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("Code","Código",0),
            FormColumns("Name","Descrição",0),
            FormColumns("U_porcentagem","comissão em porcentagem",0),
            FormColumns("U_desconto","Desconto (%) do vendedor",0),
            FormColumns("U_regressiva","Comissão regressiva?",0),
            FormColumns("U_desconto","Desconto (%)",1),
            FormColumns("U_juros","Juros (%)",1),
            FormColumns("U_prazo","Prazo",1),
        ))
        ud.setMenu(43541,1)
        userObjectsMDService.findOrCreate(ud)
    }


    fun delete(ud : UserDefinedObject) : OData? {
        val predicates = mutableListOf(
            Predicate("Code",ud.Code, Condicao.EQUAL),
        )
        val result = userObjectsMDService.get(Filter(predicates))
        if(!result.tryGetValues<UserDefinedObject>().isEmpty()){
            println("Excluindo code objet")
            return userObjectsMDService.delete("'${ud.Code}'")
        }
        return null

    }
}