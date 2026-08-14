package br.andrew.sap.model.dto

//vinculo do usuario logado com um vendedor (SalesPersons) - so preenchido quando
//User.origin == UserOriginEnum.SalePerson (ver IndexController.me())
data class VendedorVinculadoDto(
    val salesEmployeeCode: Int,
    val salesEmployeeName: String
)

data class MeuUsuarioDto(
    val id: String,
    val userName: String,
    val emailAddress: String?,
    val origin: String,
    val bussinesPlace: List<Int>,
    val roles: List<String>,
    val vendedor: VendedorVinculadoDto?
)
