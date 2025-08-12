package br.andrew.sap.model.sap

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.authentication.UserSource
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class SalePerson(val SalesEmployeeCode: Int,
                 val SalesEmployeeName : String,
                 private val Email: String?,
                 val U_filial: String?,
                 val U_envia_relatorio: String,
                 val Active: String) : UserSource {

    var u_password: String? = null
    val U_Integracao_sovis: String? = null


    @JsonIgnore
    fun getEmailAddress(): String {
        if(this.SalesEmployeeCode == -1 || Email == null)
            return emailDefault
        else
            return this.Email
    }

    @JsonIgnore
    override fun getOrigin(): UserOriginEnum {
        return UserOriginEnum.SalePerson
    }

    @JsonIgnore
    override fun getEmailAdress(): String?{
        return Email
    }

    @JsonIgnore
    override fun getId(): String {
        return SalesEmployeeCode.toString()
    }

    @JsonIgnore
    override fun getUserName(): String {
        return getEmailAddress()
    }

    @JsonIgnore
    override fun getFullName(): String {
        return SalesEmployeeName
    }

    @JsonIgnore
    override fun getPassword(): String? {
        return u_password
    }

    companion object{
        @JsonIgnore
        var emailDefault : String = "";
    }
}

