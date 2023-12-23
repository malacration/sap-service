package br.andrew.sap.model.serasa

class Parametro(private val name : String,
                val value : String,
                val bytes : Int,
                val pad : Char = ' ',
                val left : Boolean = false){
    override fun toString(): String {
        return if(left)
            value.padStart(bytes, pad)
        else
            value.padEnd(bytes, pad)
    }


}