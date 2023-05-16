package br.andrew.sap.model.partner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
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
//    var addrType : String? = null

    var addressType: AddresType? = null
        set(value) {
            if(addressName == null)
                addressName = value?.cardType
            field = value
//            addrType = value?.name
        }
    var typeOfAddress: String? = null
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
//        newAddress.addrType = this.addrType
        return newAddress
    }
}

enum class AddresType(val cardType: String) {
    bo_ShipTo("ENTREGA"),
    bo_BillTo("COBRANCA");
}