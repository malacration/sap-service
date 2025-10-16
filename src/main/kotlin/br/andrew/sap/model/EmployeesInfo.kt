package br.andrew.sap.model

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.authentication.UserSource
import br.andrew.sap.model.sap.SalePerson
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.jayway.jsonpath.internal.function.sequence.Last

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class EmployeesInfo(val EmployeeID : Int, val Active: String) : UserSource {

    var LastName : String? = null
    var FirstName : String? = null
    var MiddleName : String? = null
    var U_password : String? = null

    @JsonProperty("eMail")
    var eMail: String? = null

    @JsonIgnore
    override fun getOrigin(): UserOriginEnum {
        return UserOriginEnum.EmployeesInfo
    }

    @JsonIgnore
    override fun getId(): String {
        return EmployeeID.toString()
    }

    @JsonIgnore
    override fun getUserName(): String {
        return eMail?:throw Exception("Nao e possivel obter o email do empregado - $EmployeeID")
    }

    @JsonProperty
    override fun getFullName(): String {
        return "$FirstName $MiddleName $LastName"
    }

    @JsonIgnore
    override fun getPassword(): String? {
        return U_password
    }
}