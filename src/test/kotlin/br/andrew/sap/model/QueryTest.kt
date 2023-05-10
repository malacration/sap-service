package br.andrew.sap.model

import org.junit.jupiter.api.Test

class QueryTest {

    @Test
    fun test(){
        val sql = "SELECT * FROM table WHERE id = :id AND name = :name"
        Query("code","code",sql)
    }
}