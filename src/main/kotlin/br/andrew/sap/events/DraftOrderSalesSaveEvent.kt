package br.andrew.sap.events

import br.andrew.sap.model.documents.OrderSales
import org.springframework.context.ApplicationEvent

class DraftOrderSalesSaveEvent(val order : OrderSales) : ApplicationEvent(order) {
}