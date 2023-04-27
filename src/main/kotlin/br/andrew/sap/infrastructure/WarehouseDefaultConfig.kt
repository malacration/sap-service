package br.andrew.sap.infrastructure

import br.andrew.sap.model.WarehouseDefault
import br.andrew.sap.services.BussinesPlaceService
import org.springframework.context.annotation.Configuration

@Configuration
class WarehouseDefaultConfig(val bussinePlace : BussinesPlaceService) {

    companion object{
        var warehouses : List<WarehouseDefault> = listOf()
    }

    init {
        warehouses = bussinePlace.get().tryGetValues<WarehouseDefault>()
    }
}