package br.andrew.sap.model.serasa

import br.andrew.sap.model.partner.CpfCnpj

class B49C(val cpfCnpj: CpfCnpj, val score: Boolean = false){

    val valores = listOf(
        Parametro("nome","B49C", 4),
        Parametro("FILLER","", 6),
        Parametro("numDoc",cpfCnpj.value, 15,'0', true),
        Parametro("tipoPessoa",tipoPessoa(), 1),
        Parametro("BASE CONS","C", 6),
        Parametro("MODALIDADE","FI", 2),
        Parametro("VLR CONSUL","", 7),
        Parametro("CENTRO CUST","", 12),
        Parametro("CODIFICADO","S", 1),
        Parametro("QTD REG","99", 2),
        Parametro("CONVERSA","S", 1),
        Parametro("FUNÇÃO","INI", 3),
        Parametro("TP CONSULTA","A", 1),
        Parametro("ATUALIZA","N", 1),
        Parametro("Uso da SERASA","", 30),
        Parametro("Distribuida","", 1),
        Parametro("CONS.COLLEC","N", 1),
        Parametro("Filtro","", 57),
        Parametro("ConsultanteCnj","", 15),
        Parametro("F1","", 234),
    )

    fun tipoPessoa(): String {
        return if(cpfCnpj.isCnpj())
            "J"
        else
            "F"
    }

    override fun toString(): String {
        var chave = ""
        var valor = ""
        if(score && cpfCnpj.isCnpj()){
            chave = "RSHI"
            valor = ""
        }else if (score && cpfCnpj.isCpf()){
            chave = "RSHM"
            valor = ""
        }

        return valores.joinToString("") { it.toString() }
            .plus(P002("P002",chave,valor).resultado)
            .plus(I001("I001","00").resultado)
            .plus(Parametro("final","T999", 4, ' ', true))
    }


}
