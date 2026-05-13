package br.andrew.sap.model.entity

enum class DbSubType(val descricao: String) {
    st_None("No special sub-type."),
    st_Address("Address format."),
    st_Phone("Phone format."),
    st_Time("Time format."),
    st_Rate("Double format with the system's rate accuracy."),
    st_Sum("Double format with the system's summery accuracy."),
    st_Price("Double format with the system's price accuracy."),
    st_Quantity("Double format with the system's quantity accuracy."),
    st_Percentage("Double format with the system's percentage accuracy."),
    st_Measurement("Double format with the system's measurement accuracy."),
    st_Link("Link format (mostly used for a web site links)."),
    st_Image("Image format");

    override fun toString(): String = name // ex: "st_Measurement"
}