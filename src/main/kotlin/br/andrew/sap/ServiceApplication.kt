package br.andrew.sap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableCaching
class ServiceApplication

fun main(args: Array<String>) {
	runApplication<ServiceApplication>(*args)
}
