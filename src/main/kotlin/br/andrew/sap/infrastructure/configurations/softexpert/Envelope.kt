package br.andrew.sap.infrastructure.configurations.softexpert

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "TYPES")

class Envelope() {

    @XmlElement(name = "BODY")
    var body : Any? = null
}