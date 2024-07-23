package br.andrew.sap.model.exceptions

import br.andrew.sap.model.sap.SapError

class NotFoundException(error: SapError) : SapGenericException(error, error.throwable,"Nenhum registro correspondente encontrado (ODBC -2028) - ${error.entry}") {

}