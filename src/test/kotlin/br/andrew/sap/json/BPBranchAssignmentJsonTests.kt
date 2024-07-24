package br.andrew.sap.json

import br.andrew.sap.model.sap.partner.BPBranchAssignment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BPBranchAssignmentJsonTests  {
    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val bplid = BPBranchAssignment().also { it.bplid = "2" }
        Assertions.assertEquals("{\"BPLID\":\"2\"}",mapper.writeValueAsString(bplid))
    }
}
