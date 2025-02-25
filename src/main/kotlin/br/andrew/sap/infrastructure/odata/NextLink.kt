package br.andrew.sap.infrastructure.odata

class NextLink<T>(val content : List<T>, val nextLink : String) {
    fun hasNext() : Boolean{
        return nextLink.isNotEmpty()
    }
}