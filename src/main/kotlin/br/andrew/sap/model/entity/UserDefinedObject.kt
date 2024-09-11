package br.andrew.sap.model.entity

import br.andrew.sap.model.enums.YesNo
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.itextpdf.layout.element.Table
import org.apache.commons.lang3.mutable.Mutable

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
        (UserObjectMD_ChildTables as MutableList).add(table.also { it.SonNumber = size+1 })
    }

    fun popChildTable(vararg table : ChildTables){
        table.forEach {
            popChildTable(it)
        }
    }


    private var _UserObjectMD_FindColumns : MutableList<FindColumns> = mutableListOf<FindColumns>().also {
            if(ObjectType.getId() == "DocEntry")
                it.add(FindColumns("DocNum","DocNum"))
            else
                it.add(FindColumns(ObjectType.getId(),ObjectType.getId()))
        }
    var UserObjectMD_FindColumns : MutableList<FindColumns> = mutableListOf()
        get() = _UserObjectMD_FindColumns.also {
            it.forEachIndexed { index, findColumns ->
                findColumns.Code = this.Code
                findColumns.ColumnNumber = index+1
            }
        }

    private var _UserObjectMD_FormColumns : MutableList<FormColumns> = mutableListOf()

    var UserObjectMD_FormColumns : MutableList<FormColumns> = mutableListOf()
        get() = _UserObjectMD_FormColumns.also {
            it.forEachIndexed { index, findColumns ->
                findColumns.Code = this.Code
                findColumns.FormColumnNumber = index+1
            }
        }

    fun getUserObjectMD_EnhancedFormColumns() : List<EnhancedForm> {
        return if(this.EnableEnhancedForm == YesNo.tYES)
            UserObjectMD_ChildTables
                .flatMapIndexed { index: Int, value: ChildTables -> value.getEnhancedForm(ObjectType,index+1) }
                .toMutableList()
                .also {
                    it.addAll(
                        UserObjectMD_FormColumns
                            .filter { fil -> !it.any {
                                    fil.FormColumnAlias == it.ColumnAlias && (it.ChildNumber ?: (0 - 1)) == fil.SonNumber
                                }
                            }
                            .map { it.getEnhancedForm() }
                            .filter { it.ChildNumber != 0 }
                    )

                    it.forEachIndexed { index, it ->
                        it.Code = this.Code
                        it.ColumnNumber = index+1
                    }
                }
        else
            listOf()
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
class ChildTables(val TableName : String, val code : String? = null){

    var SonNumber: Int? = null
    val ObjectName : String = TableName

    fun getEnhancedForm(type: UDOObjType, childNumber: Int) : List<EnhancedForm> {

        return mutableListOf(
            EnhancedForm(type.getId(),type.getId()),
            EnhancedForm("LineId","LineId"),
            EnhancedForm("VisOrder","VisOrder"),
            EnhancedForm("Object","Object"),
            EnhancedForm("LogInst","LogInst"),
        ).also {
            it.forEach{
                it.ChildNumber = childNumber
                it.ColumnIsUsed = YesNo.tNO
            }
        }
    }
}


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class FormColumns(val FormColumnAlias : String, val FormColumnDescription : String, val SonNumber : Int = 0){
    var FormColumnNumber : Int? = null
    var Editable : YesNo = if(FormColumnAlias == "DocEntry" || FormColumnAlias == "DocNum") YesNo.tNO else YesNo.tYES
    var Code : String? = null


    constructor(FormColumnAlias : String, FormColumnDescription : String, SonNumber : Int, ud : UserDefinedObject)
            : this(FormColumnAlias, FormColumnDescription, SonNumber){
        this.Code = ud.Code
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

    var Code : String? = null
    var ColumnNumber : Int? = null
    @JsonIgnore
    fun getEnhancedForm() : EnhancedForm{
        return EnhancedForm(ColumnAlias,ColumnDescription).also { Code = Code }
    }
}


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class EnhancedForm(val ColumnAlias : String, val ColumnDescription : String){
    var Editable: YesNo = YesNo.tNO
    var ChildNumber : Int? = null
    var ColumnIsUsed : YesNo = YesNo.tYES

    var Code : String? = null
    var ColumnNumber : Int? = null

}

enum class UDOObjType {
    boud_MasterData, boud_Document;

    fun getId() : String{
        return if(this == boud_MasterData)  "Code"  else "DocEntry"
    }
}