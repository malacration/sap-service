package br.andrew.sap.model.exceptions

import br.andrew.sap.model.sap.SapError

open class SapGenericException(val erro : SapError, val causa : Throwable? = null, extra : String = "") :
        Exception("Entry: ${erro.entry} - "+erro.error.message.value+" - "+extra,causa)


