package br.andrew.sap.model.entity

import br.andrew.sap.model.enums.YesNo
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class UserDefinedObject(
    val Code: String,
    val Name: String,
    val TableName: String,
    val ObjectType: UDOObjType = UDOObjType.boud_MasterData,
    val CanCreateDefaultForm: YesNo = YesNo.tYES,
    val CanCancel: YesNo = YesNo.tNO,
    val CanClose: YesNo = YesNo.tNO,
    val CanArchive: YesNo = YesNo.tNO,
    val CanDelete: YesNo = YesNo.tYES,
    val CanLog: YesNo = YesNo.tYES,
    val CanYearTransfer: YesNo = YesNo.tNO,
    val CanFind: YesNo = YesNo.tYES,
    val EnableEnhancedForm: YesNo = YesNo.tYES,
    val ManageSeries: YesNo = YesNo.tNO
){
    var FormSRF: String? = null
    var UserObjectMD_ChildTables : MutableList<ChildTables> = mutableListOf()

    var UserObjectMD_FindColumns : MutableList<FindColumns> = mutableListOf(FindColumns(this.ObjectType.getId(),"código"))
    var UserObjectMD_FormColumns : MutableList<FormColumns> = mutableListOf()

    fun getUserObjectMD_EnhancedFormColumns() : List<EnhancedForm> {
        if(false)
            return UserObjectMD_FormColumns.map { it.getEnhancedForm() }.filter { it.ChildNumber != 0 }.toMutableList().also {
                it.addAll(this.UserObjectMD_FindColumns.map { it.getEnhancedForm() })
                it.add(EnhancedForm(this.ObjectType.getId(),"código").also { it.ChildNumber = 0 })
            }
        return UserObjectMD_ChildTables
            .flatMapIndexed { index: Int, value: ChildTables -> value.getEnhancedForm(ObjectType,index+1) }
            .toMutableList()
            .also {
                it.addAll(UserObjectMD_FormColumns.map { it.getEnhancedForm() }.filter { it.ChildNumber != 0 })
            }
    }

    var RebuildEnhancedForm: YesNo = YesNo.tYES

    var FatherMenuID : Int? = null
    var Position : Int? = null
    var MenuUID : String = Name
    var MenuCaption : String? = Name
    var MenuItem : YesNo? = null

    fun setMenu(FatherMenuID : Int, position : Int){
        this.FatherMenuID = FatherMenuID
        this.Position = position
        this.MenuItem = YesNo.tYES
    }
}


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ChildTables(val TableName : String){
    fun getEnhancedForm(type: UDOObjType, childNumber: Int) : List<EnhancedForm> {
        return listOf(
            EnhancedForm(type.getId(),"Codigo").also { it.ChildNumber = childNumber },
            EnhancedForm("LineId","LineId").also { it.ChildNumber = childNumber },
            EnhancedForm("Object","Object").also { it.ChildNumber = childNumber },
            EnhancedForm("LogInst","LogInst").also { it.ChildNumber = childNumber },
        )
    }
}


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class FormColumns(val FormColumnAlias : String, val FormColumnDescription : String, val SonNumber : Int = 0){
    var FormColumnNumber : Int? = null
    var Editable : YesNo = YesNo.tYES


    @JsonIgnore
    fun getEnhancedForm() : EnhancedForm{
        return EnhancedForm(FormColumnAlias,FormColumnDescription).also { it.ChildNumber = SonNumber }
    }
}


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class FindColumns(val ColumnAlias : String, val ColumnDescription : String){

    @JsonIgnore
    fun getEnhancedForm() : EnhancedForm{
        return EnhancedForm(ColumnAlias,ColumnDescription)
    }
}


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class EnhancedForm(val ColumnAlias : String, val ColumnDescription : String){
    var ChildNumber : Int? = null
}

enum class UDOObjType {
    boud_MasterData, boud_Document;

    fun getId() : String{
        return if(this == boud_MasterData)  "Code"  else "DocEntry"
    }
}