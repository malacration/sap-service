package br.andrew.sap.rovema

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RovemaApplication

fun main(args: Array<String>) {
	runApplication<RovemaApplication>(*args)
}
