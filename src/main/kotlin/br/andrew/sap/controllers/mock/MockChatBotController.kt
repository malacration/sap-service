package br.andrew.sap.controllers.mock

import io.swagger.v3.oas.annotations.Parameter
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date


@RestController
@RequestMapping("mock")
class MockChatBotController {


    /**
     * Observar que esse modelo o ID do cliente é igual ao CPF/CNPJ
     * Porem no modelo do sap isso nao é verdade
     */

    @GetMapping("/cliente/cpf-cnpj/{cpfCnpj}")
    fun getCliente(@Parameter(name = "ola") @PathVariable cpfCnpj : String): Cliente {
        return clientes.first { it.cpfCnpj == cpfCnpj }
    }

    @GetMapping("/cliente/id/{id}")
    fun getClienteId(@Parameter() @PathVariable id : String): Any {
        return clientes.first { it.id == id }
    }

    @GetMapping("/fatura/{idFatura}")
    fun getFatura(@Parameter() @PathVariable id : String): Any{
        return this.faturas.first{it.id == id}
    }

    @GetMapping("/fatura/{idFatura}/boleto")
    fun getBoletoByFatura(): ByteArrayResource {
        val pdf = File("src/main/kotlin/br/andrew/sap/controllers/mock/semple.pdf").readBytes()
        return ByteArrayResource(pdf)
    }

    fun headerValidation(header : String){
        if(header!="windson")
            throw Exception("Acesso negado - mock");
    }

    val faturas = listOf(
            Fatura("1","1","3344",
                    getDate("2023-01-01"),
                    getDate("2023-01-01"),100.0),
            Fatura("2","1","3344",
                    Date(),
                    Date(),136.20),
            Fatura("3","2","5532",
                    Date(),
                    Date(),666.66)
    )

    val clientes = listOf<Cliente>(
            Cliente("1","cpf-1","Windson Chico Francisco",this.faturas.filter { it.idCliente == "1" }),
            Cliente("2","cpf-2","Fernanda Jeovania", this.faturas.filter { it.idCliente == "2" })
    )

    data class Cliente(val id : String, val cpfCnpj : String, val name: String, val faturas : List<Fatura>)

    data class Fatura(val id : String, val idCliente : String, val idNfe : String, val dueDate : Date,
                      val createDate: Date, val value : Double)

    fun getDate(entrada : String): Date {
        return Date.from(LocalDate.parse(entrada).atStartOfDay(ZoneId.systemDefault()).toInstant())
    }


}