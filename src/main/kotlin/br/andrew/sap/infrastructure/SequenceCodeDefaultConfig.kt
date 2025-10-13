//package br.andrew.sap.infrastructure
//
//import br.andrew.sap.model.WarehouseDefault
//import br.andrew.sap.services.BussinessPlaceService
//import br.andrew.sap.services.TelegramRequestService
//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Profile
//
//@Configuration
//@Profile("!test")
//class SequenceCodeDefaultConfig(
//    bussinePlace : BussinessPlaceService,
//    @Value("\${sequence.model:39}") val wareHouseToleranceFail : Boolean
//) {
//
//    val logger: Logger = LoggerFactory.getLogger(SequenceCodeDefaultConfig::class.java)
//
//    companion object{
//        var warehouses : List<WarehouseDefault> = listOf()
//    }
//
//    init {
//        try {
//            warehouses = bussinePlace.get().tryGetValues<WarehouseDefault>()
//        }catch (x : Exception){
//            if (!wareHouseToleranceFail)
//                throw x
//            else
//                logger.error("Erro ao carregar depositos padroes",x)
//        }
//    }
//}