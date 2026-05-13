package br.andrew.sap.model.entity

enum class DbType(val size : Int?){
    db_Alpha(254),
    db_Memo(null), //Observacao
    db_Numeric(null),
    db_Float(null),
    db_Date(null);
}