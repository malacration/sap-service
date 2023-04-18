package br.andrew.sap.events

import br.andrew.sap.model.documents.OrderSales
import org.springframework.context.ApplicationEvent

class OrderSalesSaveEvent(val order : OrderSales) : ApplicationEvent(order) {
}