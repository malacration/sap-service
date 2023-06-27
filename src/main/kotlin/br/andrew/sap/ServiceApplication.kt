package br.andrew.sap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServiceApplication

fun main(args: Array<String>) {
	runApplication<br.andrew.sap.ServiceApplication>(*args)

	/**
	 * Classe utilizada para demonstrar o uso de
	 * métodos que recebem parametros.
	 */
	/**
	 * Classe utilizada para demonstrar o uso de
	 * métodos que recebem parametros.
	 */
	class MetodoParametro {
		/* Declaração dos atributos da classe. */
		var atributo1 = 0
		/* Declaração dos métodos da classe. */
		/**
		 * Método utilizado para atribuir o valor do atributo1.
		 */
		fun metodo1(valor: Int) {
			println("Chamando o metodo 1.")
			atributo1 = valor
			println("O valor do atributo1 eh: $atributo1")
		}

		/**
		 * Método que recebe uma quantidade de parametros variados
		 * e imprime todos os valores recebidos.
		 * Essa possibilidade de receber uma quantidade de parametros
		 * variados é chamado de varargs e foi implementado a partir
		 * da versão 5.0 do java.
		 */
		fun metodo2(vararg valores: Int) {
			println("Chamando o método 2.")
			/* Verifica se recebeu algum argumento. */if (valores.size > 0) {
				/* Para cada argumento recebido como parametro, imprime seu valor. */
				for (cont in valores.indices) {
					val valor = valores[cont]
					print("$valor ")
				}
				println("\n")

				/* Este for faz a mesma coisa que o anterior, este novo tipo de for
         chamado foreach foi implementado a partir da versão 5.0 do java. */for (valor in valores) {
					print("$valor ")
				}
				println("\n")
			}
		}
	}
}
