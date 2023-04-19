package br.andrew.sap.listener

import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.model.documents.OrderSales
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher


@SpringBootTest
class OrderSalesSaveListenerTests {

    @Autowired
    var applicationEventPublisher: ApplicationEventPublisher? = null

    @Test
    fun test(){
        val order = OrderSales("windson","", listOf(),"3")
            .also {
                it.docEntry = 10
                it.docNum = "11"
            }
        try {
            applicationEventPublisher?.publishEvent(OrderSalesSaveEvent(order))
        }catch (t : Throwable){
            t.printStackTrace()
        }
    }
}