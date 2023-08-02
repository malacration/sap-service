package br.andrew.sap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ServiceApplication

fun main(args: Array<String>) {
	runApplication<ServiceApplication>(*args)
}
