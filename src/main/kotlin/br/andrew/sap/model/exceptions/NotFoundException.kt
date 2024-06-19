package br.andrew.sap.model.exceptions

import br.andrew.sap.infrastructure.configurations.EventPublisherSingleton
import br.andrew.sap.model.SapError
import br.andrew.sap.model.documents.base.Document

class NotFoundException(error: SapError) : SapGenericException(error, error.throwable,"Nenhum registro correspondente encontrado (ODBC -2028) - ${error.entry}") {

}