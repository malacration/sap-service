package br.andrew.sap.json

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.documents.Invoice
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DocumentLinesTestsJsonTests {

    @Test
    fun tryJsonWithProduct(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(jsonProduct, jacksonTypeRef<OData>())
        val data = obj.tryGetValue<Invoice>()
        Assertions.assertEquals(666,data.docEntry)
        val product = data.DocumentLines.get(0)
        Assertions.assertEquals(0,product.LineNum)
        Assertions.assertEquals("PAC0000100",product.ItemCode)
        //assert all properties
        Assertions.assertEquals("176.0",product.Quantity)
//        Assertions.assertEquals(77.44,product.price)
//        Assertions.assertEquals(98.1552,product.priceAfterVAT)
//        Assertions.assertEquals("R\$",product.currency)
//        Assertions.assertEquals(0.0,product.rate)
        Assertions.assertEquals(12.0,product.DiscountPercent)
        Assertions.assertEquals("501.06",product.WarehouseCode)
//        Assertions.assertEquals(30,product.salesPersonCode)
        Assertions.assertEquals("500",product.CostingCode)
        Assertions.assertEquals("50000103",product.CostingCode2)
        Assertions.assertEquals("3.1.1.001.00001",product.AccountCode)
        Assertions.assertEquals(17,product.Usage)
        Assertions.assertEquals(0.0,product.U_preco_base)
        Assertions.assertEquals(0.0,product.U_preco_negociado)
        Assertions.assertEquals(null,product.U_id_item_forca)
        Assertions.assertEquals("TESTE01",product.TaxCode)
    }

    @Test
    fun tryJsonWithService(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(jsonService, jacksonTypeRef<OData>())
        val data = obj.tryGetValue<Invoice>()
        Assertions.assertEquals(333,data.docEntry)
        val service = data.DocumentLines.get(0)
        Assertions.assertEquals(0,service.LineNum)

        Assertions.assertEquals(null,service.ItemCode)
        Assertions.assertEquals("0.0",service.Quantity)
//        Assertions.assertEquals(77.44,product.price)
//        Assertions.assertEquals(98.1552,product.priceAfterVAT)
//        Assertions.assertEquals("R\$",product.currency)
//        Assertions.assertEquals(0.0,product.rate)
        Assertions.assertEquals(0.0,service.DiscountPercent)
        Assertions.assertEquals(null,service.WarehouseCode)
//        Assertions.assertEquals(30,product.salesPersonCode)
        Assertions.assertEquals(null,service.CostingCode)
        Assertions.assertEquals(null,service.CostingCode2)
        Assertions.assertEquals("2.9.1.001.00006",service.AccountCode)
        Assertions.assertEquals(9,service.Usage)
        Assertions.assertEquals(null,service.U_preco_base)
        Assertions.assertEquals(null,service.U_preco_negociado)
        Assertions.assertEquals(null,service.U_id_item_forca)
        Assertions.assertEquals(null,service.TaxCode)
    }

    val jsonProduct = "{" +
            "\"DocEntry\": 666," +
            "\"CardCode\": \"CLI0001\"," +
            "\"BPL_IDAssignedToInvoice\": \"CLI0001\"," +
            "\"DocumentLines\": [\n" +
            "        {\n" +
            "          \"LineNum\": 0,\n" +
            "          \"ItemCode\": \"PAC0000100\",\n" +
            "          \"ItemDescription\": \"OX 40 ENGORDA 30KG\",\n" +
            "          \"Quantity\": 176.0,\n" +
            "          \"ShipDate\": \"2023-07-05\",\n" +
            "          \"Price\": 77.44,\n" +
            "          \"PriceAfterVAT\": 98.1552,\n" +
            "          \"Currency\": \"R\$\",\n" +
            "          \"Rate\": 0.0,\n" +
            "          \"DiscountPercent\": 12.0,\n" +
            "          \"VendorNum\": \"\",\n" +
            "          \"SerialNum\": \"\",\n" +
            "          \"WarehouseCode\": \"501.06\",\n" +
            "          \"SalesPersonCode\": 30,\n" +
            "          \"CommisionPercent\": 0.0,\n" +
            "          \"TreeType\": \"iProductionTree\",\n" +
            "          \"AccountCode\": \"3.1.1.001.00001\",\n" +
            "          \"UseBaseUnits\": \"tNO\",\n" +
            "          \"SupplierCatNum\": \"\",\n" +
            "          \"CostingCode\": \"500\",\n" +
            "          \"ProjectCode\": \"\",\n" +
            "          \"BarCode\": null,\n" +
            "          \"VatGroup\": \"\",\n" +
            "          \"Height1\": 0.0,\n" +
            "          \"Hight1Unit\": null,\n" +
            "          \"Height2\": 0.0,\n" +
            "          \"Height2Unit\": null,\n" +
            "          \"Lengh1\": 0.0,\n" +
            "          \"Lengh1Unit\": null,\n" +
            "          \"Lengh2\": 0.0,\n" +
            "          \"Lengh2Unit\": null,\n" +
            "          \"Weight1\": 5280.0,\n" +
            "          \"Weight1Unit\": 3,\n" +
            "          \"Weight2\": 0.0,\n" +
            "          \"Weight2Unit\": null,\n" +
            "          \"Factor1\": 1.0,\n" +
            "          \"Factor2\": 1.0,\n" +
            "          \"Factor3\": 1.0,\n" +
            "          \"Factor4\": 1.0,\n" +
            "          \"BaseType\": -1,\n" +
            "          \"BaseEntry\": null,\n" +
            "          \"BaseLine\": null,\n" +
            "          \"Volume\": 0.0,\n" +
            "          \"VolumeUnit\": 4,\n" +
            "          \"Width1\": 0.0,\n" +
            "          \"Width1Unit\": null,\n" +
            "          \"Width2\": 0.0,\n" +
            "          \"Width2Unit\": null,\n" +
            "          \"Address\": \"RODOVIAAC-40,261-A\\rGALPÃO 02\\r69.908-732-RIO BRANCO-AC\\rBRASIL\",\n" +
            "          \"TaxCode\": \"TESTE01\",\n" +
            "          \"TaxType\": \"tt_Yes\",\n" +
            "          \"TaxLiable\": \"tYES\",\n" +
            "          \"PickStatus\": \"tNO\",\n" +
            "          \"PickQuantity\": 0.0,\n" +
            "          \"PickListIdNumber\": null,\n" +
            "          \"OriginalItem\": null,\n" +
            "          \"BackOrder\": \"tYES\",\n" +
            "          \"FreeText\": \"\",\n" +
            "          \"ShippingMethod\": -1,\n" +
            "          \"POTargetNum\": null,\n" +
            "          \"POTargetEntry\": \"\",\n" +
            "          \"POTargetRowNum\": null,\n" +
            "          \"CorrectionInvoiceItem\": \"ciis_ShouldBe\",\n" +
            "          \"CorrInvAmountToStock\": 0.0,\n" +
            "          \"CorrInvAmountToDiffAcct\": 0.0,\n" +
            "          \"AppliedTax\": 0.0,\n" +
            "          \"AppliedTaxFC\": 0.0,\n" +
            "          \"AppliedTaxSC\": 0.0,\n" +
            "          \"WTLiable\": \"tNO\",\n" +
            "          \"DeferredTax\": \"tNO\",\n" +
            "          \"EqualizationTaxPercent\": 0.0,\n" +
            "          \"TotalEqualizationTax\": 0.0,\n" +
            "          \"TotalEqualizationTaxFC\": 0.0,\n" +
            "          \"TotalEqualizationTaxSC\": 0.0,\n" +
            "          \"NetTaxAmount\": 0.0,\n" +
            "          \"NetTaxAmountFC\": 0.0,\n" +
            "          \"NetTaxAmountSC\": 0.0,\n" +
            "          \"MeasureUnit\": \"SACA 30KG\",\n" +
            "          \"UnitsOfMeasurment\": 1.0,\n" +
            "          \"LineTotal\": 13629.44,\n" +
            "          \"TaxPercentagePerRow\": 26.75,\n" +
            "          \"TaxTotal\": 0.0,\n" +
            "          \"ConsumerSalesForecast\": \"tYES\",\n" +
            "          \"ExciseAmount\": 0.0,\n" +
            "          \"TaxPerUnit\": 0.0,\n" +
            "          \"TotalInclTax\": 0.0,\n" +
            "          \"CountryOrg\": null,\n" +
            "          \"SWW\": \"\",\n" +
            "          \"TransactionType\": null,\n" +
            "          \"DistributeExpense\": \"tYES\",\n" +
            "          \"RowTotalFC\": 0.0,\n" +
            "          \"RowTotalSC\": 13629.44,\n" +
            "          \"LastBuyInmPrice\": 0.0,\n" +
            "          \"LastBuyDistributeSumFc\": 0.0,\n" +
            "          \"LastBuyDistributeSumSc\": 0.0,\n" +
            "          \"LastBuyDistributeSum\": 0.0,\n" +
            "          \"StockDistributesumForeign\": 0.0,\n" +
            "          \"StockDistributesumSystem\": 0.0,\n" +
            "          \"StockDistributesum\": 0.0,\n" +
            "          \"StockInmPrice\": 0.0,\n" +
            "          \"PickStatusEx\": \"dlps_NotPicked\",\n" +
            "          \"TaxBeforeDPM\": 0.0,\n" +
            "          \"TaxBeforeDPMFC\": 0.0,\n" +
            "          \"TaxBeforeDPMSC\": 0.0,\n" +
            "          \"CFOPCode\": \"6116\",\n" +
            "          \"CSTCode\": \"0.00\",\n" +
            "          \"Usage\": 17,\n" +
            "          \"TaxOnly\": \"tNO\",\n" +
            "          \"VisualOrder\": 0,\n" +
            "          \"BaseOpenQuantity\": 0.0,\n" +
            "          \"UnitPrice\": 88.0,\n" +
            "          \"LineStatus\": \"bost_Open\",\n" +
            "          \"PackageQuantity\": 176.0,\n" +
            "          \"Text\": \"\",\n" +
            "          \"LineType\": \"dlt_Regular\",\n" +
            "          \"COGSCostingCode\": \"500\",\n" +
            "          \"COGSAccountCode\": \"4.1.1.001.00001\",\n" +
            "          \"ChangeAssemlyBoMWarehouse\": \"N\",\n" +
            "          \"GrossBuyPrice\": 100.368,\n" +
            "          \"GrossBase\": -1,\n" +
            "          \"GrossProfitTotalBasePrice\": 17664.77,\n" +
            "          \"CostingCode2\": \"50000103\",\n" +
            "          \"CostingCode3\": null,\n" +
            "          \"CostingCode4\": null,\n" +
            "          \"CostingCode5\": null,\n" +
            "          \"ItemDetails\": null,\n" +
            "          \"LocationCode\": null,\n" +
            "          \"ActualDeliveryDate\": null,\n" +
            "          \"RemainingOpenQuantity\": 0.0,\n" +
            "          \"OpenAmount\": 13629.44,\n" +
            "          \"OpenAmountFC\": 0.0,\n" +
            "          \"OpenAmountSC\": 13629.44,\n" +
            "          \"ExLineNo\": null,\n" +
            "          \"RequiredDate\": null,\n" +
            "          \"RequiredQuantity\": 0.0,\n" +
            "          \"COGSCostingCode2\": \"50000103\",\n" +
            "          \"COGSCostingCode3\": null,\n" +
            "          \"COGSCostingCode4\": null,\n" +
            "          \"COGSCostingCode5\": null,\n" +
            "          \"CSTforIPI\": \"53\",\n" +
            "          \"CSTforPIS\": \"01\",\n" +
            "          \"CSTforCOFINS\": \"01\",\n" +
            "          \"CreditOriginCode\": \"0\",\n" +
            "          \"WithoutInventoryMovement\": \"tNO\",\n" +
            "          \"AgreementNo\": null,\n" +
            "          \"AgreementRowNumber\": null,\n" +
            "          \"ActualBaseEntry\": null,\n" +
            "          \"ActualBaseLine\": null,\n" +
            "          \"DocEntry\": 16781,\n" +
            "          \"Surpluses\": 0.0,\n" +
            "          \"DefectAndBreakup\": 0.0,\n" +
            "          \"Shortages\": 0.0,\n" +
            "          \"ConsiderQuantity\": \"tNO\",\n" +
            "          \"PartialRetirement\": \"tNO\",\n" +
            "          \"RetirementQuantity\": 0.0,\n" +
            "          \"RetirementAPC\": 0.0,\n" +
            "          \"ThirdParty\": \"tNO\",\n" +
            "          \"PoNum\": null,\n" +
            "          \"PoItmNum\": null,\n" +
            "          \"ExpenseType\": null,\n" +
            "          \"ReceiptNumber\": null,\n" +
            "          \"ExpenseOperationType\": null,\n" +
            "          \"FederalTaxID\": null,\n" +
            "          \"GrossProfit\": -4035.33,\n" +
            "          \"GrossProfitFC\": 0.0,\n" +
            "          \"GrossProfitSC\": -4035.33,\n" +
            "          \"PriceSource\": \"dpsManual\",\n" +
            "          \"StgSeqNum\": null,\n" +
            "          \"StgEntry\": null,\n" +
            "          \"StgDesc\": null,\n" +
            "          \"UoMEntry\": 4,\n" +
            "          \"UoMCode\": \"SC/30\",\n" +
            "          \"InventoryQuantity\": 176.0,\n" +
            "          \"RemainingOpenInventoryQuantity\": 0.0,\n" +
            "          \"ParentLineNum\": null,\n" +
            "          \"Incoterms\": 0,\n" +
            "          \"TransportMode\": 0,\n" +
            "          \"NatureOfTransaction\": null,\n" +
            "          \"DestinationCountryForImport\": null,\n" +
            "          \"DestinationRegionForImport\": null,\n" +
            "          \"OriginCountryForExport\": null,\n" +
            "          \"OriginRegionForExport\": null,\n" +
            "          \"ItemType\": \"dit_Item\",\n" +
            "          \"ChangeInventoryQuantityIndependently\": \"tNO\",\n" +
            "          \"FreeOfChargeBP\": \"tNO\",\n" +
            "          \"SACEntry\": null,\n" +
            "          \"HSNEntry\": null,\n" +
            "          \"GrossPrice\": 111.54,\n" +
            "          \"GrossTotal\": 17275.32,\n" +
            "          \"GrossTotalFC\": 0.0,\n" +
            "          \"GrossTotalSC\": 17275.32,\n" +
            "          \"NCMCode\": -1,\n" +
            "          \"NVECode\": null,\n" +
            "          \"IndEscala\": \"tYES\",\n" +
            "          \"CtrSealQty\": 0.0,\n" +
            "          \"CNJPMan\": null,\n" +
            "          \"CESTCode\": 980,\n" +
            "          \"UFFiscalBenefitCode\": null,\n" +
            "          \"ReverseCharge\": \"tNO\",\n" +
            "          \"ShipToCode\": \"ENTREGA\",\n" +
            "          \"ShipToDescription\": \"RODBR 364 KM 10 SAIDA PARA CUIABA,S/N\\r\\r76860000-Candeias do Jamari-RO\\rBRASIL\",\n" +
            "          \"ExternalCalcTaxRate\": 0.0,\n" +
            "          \"ExternalCalcTaxAmount\": 0.0,\n" +
            "          \"ExternalCalcTaxAmountFC\": 0.0,\n" +
            "          \"ExternalCalcTaxAmountSC\": 0.0,\n" +
            "          \"StandardItemIdentification\": null,\n" +
            "          \"CommodityClassification\": null,\n" +
            "          \"UnencumberedReason\": null,\n" +
            "          \"CUSplit\": \"tNO\",\n" +
            "          \"U_B1SYS_IVAST\": 0.0,\n" +
            "          \"U_B1SYS_TaxUoM\": null,\n" +
            "          \"U_B1SYS_QtyPerTaxUoM\": 0.0,\n" +
            "          \"U_B1SYS_RevenueInd\": \"0\",\n" +
            "          \"U_B1SYS_RevenueInd2\": \"\",\n" +
            "          \"U_B1SYS_RevenueRec\": \"\",\n" +
            "          \"U_B1SYS_POSTotICMS\": null,\n" +
            "          \"U_B1SYS_PSTotISSQN\": null,\n" +
            "          \"U_B1SYS_PTotICMSST\": null,\n" +
            "          \"U_B1SYS_CodigoDePar\": \"\",\n" +
            "          \"U_B1SYS_IPIStmpCnt\": null,\n" +
            "          \"U_LBR_Destinacao\": null,\n" +
            "          \"U_TX_CodFci\": null,\n" +
            "          \"U_TX_xCampo\": null,\n" +
            "          \"U_TX_xTexto\": null,\n" +
            "          \"U_TX_IndMovF\": null,\n" +
            "          \"U_TX_CodInfA\": null,\n" +
            "          \"U_TX_Pmc\": null,\n" +
            "          \"U_TX_vProTrib\": null,\n" +
            "          \"U_TX_IndMed\": null,\n" +
            "          \"U_TX_CodigoAuxiliar\": null,\n" +
            "          \"U_TX_UTrib\": null,\n" +
            "          \"U_TX_FatorTrib\": null,\n" +
            "          \"U_ROV_PREBASE\": \"88,0000\",\n" +
            "          \"U_NumeroBloco\": null,\n" +
            "          \"U_xPed\": null,\n" +
            "          \"U_nItem\": null,\n" +
            "          \"U_TX_IndTot\": null,\n" +
            "          \"U_Rov_CertificadoAporvacao\": null,\n" +
            "          \"U_preco_negociado\": 0.0,\n" +
            "          \"U_preco_base\": 0.0,\n" +
            "          \"U_ROV_Frete\": 0.0,\n" +
            "          \"U_ROV_TOTAL_COM_FRETE\": 0.0,\n" +
            "          \"U_id_item_forca\": null,\n" +
            "          \"U_TX_ItemKit\": null" +
            "}]" +
            "}"

    val jsonService = "{" +
            "\"DocEntry\": 333," +
            "\"CardCode\": \"CLI0001\"," +
            "\"BPL_IDAssignedToInvoice\": \"CLI0001\"," +
            "\"DocumentLines\": [\n" +
            "        {\n" +
            "          \"LineNum\": 0,\n" +
            "          \"ItemCode\": null,\n" +
            "          \"ItemDescription\": \"QUITACAO DE FATURA\",\n" +
            "          \"Quantity\": 0.0,\n" +
            "          \"ShipDate\": null,\n" +
            "          \"Price\": 10500.0,\n" +
            "          \"PriceAfterVAT\": 10500.0,\n" +
            "          \"Currency\": \"R\$\",\n" +
            "          \"Rate\": 0.0,\n" +
            "          \"DiscountPercent\": 0.0,\n" +
            "          \"VendorNum\": null,\n" +
            "          \"SerialNum\": null,\n" +
            "          \"WarehouseCode\": null,\n" +
            "          \"SalesPersonCode\": 30,\n" +
            "          \"CommisionPercent\": 0.0,\n" +
            "          \"TreeType\": \"iNotATree\",\n" +
            "          \"AccountCode\": \"2.9.1.001.00006\"" +
            "        }]" +
            "}"
}