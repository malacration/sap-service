package br.andrew.sap.infrastructure.odata

class Parameter(val key : String, val value : Any) {

    override fun toString(): String {
        return if(value is String && !value.toString().startsWith("'"))
            "$key='$value'"
        else
            "$key=$value"
    }

    companion object {

        /**
         * Termo de busca pronto para um LIKE de SQLQueries: o Service Layer recusa qualquer parametro
         * com caractere fora do ASCII ("Parameter error.", codigo 704) - buscar "RAÇÃO" derrubava a
         * busca. Cada um desses caracteres vira "_", o curinga de um caractere so do LIKE: "RA__O"
         * continua achando RAÇÃO, RAÇAO e RACAO. Tirar o acento (RACAO) nao serviria, porque o
         * cadastro grava com acento e o LIKE compara byte a byte. Trocar dentro da view tambem nao:
         * o Service Layer nao aceita REPLACE nem expressao regular no SqlText.
         */
        fun termoLike(termo : String) : String {
            return termo.map { if(it.code > 127) '_' else it }.joinToString("")
        }
    }
}
