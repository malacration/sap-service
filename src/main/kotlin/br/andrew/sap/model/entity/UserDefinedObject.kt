package br.andrew.sap.model.entity

class UserDefinedObject(
    val Name: String,
    val TableName: String,
    val ObjectType: String = "boud_Document",
    val CanCreateDefaultForm: String = "tNO",
    val CanCancel: String = "tYES",
    val CanClose: String = "tYES",
    val CanDelete: String = "tYES",
    val CanLog: String = "tNO",
    val CanYearTransfer: String = "tYES",
    val CanFind: String = "tYES",
    val ManageSeries: String = "tNO",
    val ChildTable : List<Any> = listOf()
)