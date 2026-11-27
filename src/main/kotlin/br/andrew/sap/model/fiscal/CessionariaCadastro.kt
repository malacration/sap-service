package br.andrew.sap.model.fiscal

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.http.HttpStatus

class CessionariaRequest(val nome: String? = null, val cnpj: String? = null, val telefone: String? = null, val nomePdf: String? = null)

// nomePdf: chave ausente no JSON = manter o gravado; presente (mesmo null) = gravar (vazio faz o termo usar o nome).
class CessionariaEditRequest(val nome: String? = null) {
    constructor(nome: String?, nomePdf: String?) : this(nome) { this.nomePdf = nomePdf }

    var nomePdf: String? = null
        set(valor) { field = valor; nomePdfInformado = true }

    @get:JsonIgnore
    var nomePdfInformado: Boolean = false
        private set
}

// Linha do UDO NFL_CESSIONARIA. Code = CNPJ so com digitos.
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CessionariaUdo(
    var Code: String? = null,
    var Name: String? = null,
    var U_Cnpj: String? = null,
    var U_Telefone: String? = null,
    var U_NomePdf: String? = null,
    var U_Ativo: String? = "Y"
)

// Erro de entrada da tela: o controller responde com o status (400/409) e o corpo ErroDto.
class CessionariaInvalidaException(mensagem: String, val status: HttpStatus = HttpStatus.BAD_REQUEST) : RuntimeException(mensagem)
