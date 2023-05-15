package br.andrew.sap.infrastructure.configurations

import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Configuration


@Configuration
class EventPublisherSingleton(val publisher : ApplicationEventPublisher) {

    init {
        instance = publisher
    }

    companion object {
        lateinit var instance : ApplicationEventPublisher
    }
}