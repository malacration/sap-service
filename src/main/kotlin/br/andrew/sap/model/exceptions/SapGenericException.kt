package br.andrew.sap.model.exceptions

import br.andrew.sap.model.SapError

open class SapGenericException(val erro : SapError, val causa : Throwable? = null) :
        Exception(erro.error.message.value,causa){

}