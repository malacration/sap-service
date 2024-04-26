package br.andrew.sap.model.partner

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.text.Normalizer

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Address {

    var addressName : String? = null
    var Street : String? = null
    var Block: String? = null
    var ZipCode: String? = null
    var City: String? = null
    var County: String? = null
    var Country:  String? = null
    var State: String? = null
    var BuildingFloorRoom: String? = null


    @JsonIgnoreProperties
    private var BPCode : String? = null


    @JsonProperty("BPCode")
    fun getBPCode() : String? {
        return BPCode
    }

    fun setBPCode(BPCode : String?){
        this.BPCode = BPCode
    }

    @JsonProperty("RowNum")
    var RowNum : Int? = null

    var addressType: AddresType? = null
        set(value) {
            if(addressName == null)
                addressName = value?.cardType
            field = value
//            addrType = value?.name
        }
    var typeOfAddress: String = "none"
    var StreetNo: String? = null


    //copy for new intance address
    @JsonIgnoreProperties
    fun duplicate() : Address{
        val newAddress = Address()
        newAddress.addressName = this.addressName
        newAddress.Street = this.Street
        newAddress.Block = this.Block
        newAddress.ZipCode = this.ZipCode
        newAddress.City = this.City
        newAddress.County = this.County
        newAddress.Country = this.Country
        newAddress.State = this.State
        newAddress.BuildingFloorRoom = this.BuildingFloorRoom
        newAddress.addressType = this.addressType
        newAddress.typeOfAddress = this.typeOfAddress
        newAddress.StreetNo = this.StreetNo
        newAddress.BPCode = this.BPCode
        newAddress.RowNum = this.RowNum
//        newAddress.addrType = this.addrType
        return newAddress
    }


    @JsonIgnore
    fun isValid(): Boolean {
        return normalize().equals(addressName,true)
    }

    @JsonIgnore
    fun normalize(): String? {
        return addressName
        ?.replace("[éèêë]".toRegex(RegexOption.IGNORE_CASE), "E")
        ?.replace("[áàäâã]".toRegex(RegexOption.IGNORE_CASE), "A")
        ?.replace("[úùüû]".toRegex(RegexOption.IGNORE_CASE), "U")
        ?.replace("[óòôõö]".toRegex(RegexOption.IGNORE_CASE), "O")
        ?.replace("[íìïî]".toRegex(RegexOption.IGNORE_CASE), "I")
        ?.replace("Ç".toRegex(RegexOption.IGNORE_CASE), "C")

    }
}

enum class AddresType(val cardType: String) {
    bo_ShipTo("ENTREGA"),
    bo_BillTo("COBRANCA");
}