package br.andrew.sap.model.sap.journal

//distingue "o documento de origem nao tem rateio para herdar" (recebimento por conta, NF sem
//rateio) de falha transitoria de rede/Service Layer: so a primeira autoriza cair no padrao da filial
class SemRateioNaOrigemException(message: String) : Exception(message)
