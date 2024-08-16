package br.andrew.sap.model.entity

import br.andrew.sap.model.enums.YesNo
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.itextpdf.layout.element.Table

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class UserDefinedObject(
    val Code: String,
    val Name: String,
    val TableName: String,
    val ObjectType: UDOObjType = UDOObjType.boud_MasterData,
    val CanDelete: YesNo = YesNo.tYES,
    val CanCreateDefaultForm: YesNo = YesNo.tYES,
    val CanCancel: YesNo = YesNo.tNO,
    val CanClose: YesNo = YesNo.tNO,
    val CanArchive: YesNo = YesNo.tNO,
    val CanLog: YesNo = YesNo.tYES,
    val CanYearTransfer: YesNo = YesNo.tNO,
    val CanFind: YesNo = YesNo.tYES,
    val EnableEnhancedForm: YesNo = YesNo.tYES,
    val ManageSeries: YesNo = YesNo.tNO,
    UserObjectMD_ChildTables : List<ChildTables>? = null
){
    var FormSRF: String? = null

    private var _UserObjectMD_ChildTables : MutableList<ChildTables> = mutableListOf()
    var UserObjectMD_ChildTables : List<ChildTables> = listOf()
        get() = _UserObjectMD_ChildTables
    init {
        if(UserObjectMD_ChildTables != null)
        this._UserObjectMD_ChildTables = UserObjectMD_ChildTables.toMutableList()
    }

    fun popChildTable(table : ChildTables){
        val size = UserObjectMD_ChildTables.size
        (UserObjectMD_ChildTables as MutableList).add(table.also { it.SonNumber = size })
    }

    fun popChildTable(vararg table : ChildTables){
        table.forEach {
            popChildTable(it)
        }
    }


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

    var RebuildEnhancedForm: YesNo = YesNo.tNO

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
class ChildTables(val TableName : String, val code : String){

    var SonNumber: Int? = null
    val ObjectName : String = TableName

    constructor(tableName : String, ud : UserDefinedObject) : this(tableName,ud.Code)
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
    var code : String? = null


    constructor(FormColumnAlias : String, FormColumnDescription : String, SonNumber : Int, ud : UserDefinedObject)
            : this(FormColumnAlias, FormColumnDescription, SonNumber){
        this.code = ud.Code
    }

    @JsonIgnore
    fun getEnhancedForm() : EnhancedForm{
        return EnhancedForm(FormColumnAlias,FormColumnDescription)
            .also {
                it.ChildNumber = SonNumber
                it.ColumnIsUsed = YesNo.tYES
                it.Editable = Editable
            }
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
    var Editable: YesNo = YesNo.tNO
    var ChildNumber : Int? = null
    var ColumnIsUsed : YesNo = YesNo.tNO
}

enum class UDOObjType {
    boud_MasterData, boud_Document;

    fun getId() : String{
        return if(this == boud_MasterData)  "Code"  else "DocEntry"
    }
}