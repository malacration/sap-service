package br.andrew.sap.json

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.SapUser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SapUserJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val sapUser = mapper.readValue(json,jacksonTypeRef<OData>()).tryGetValue<SapUser>()
        Assertions.assertEquals(162,sapUser.internalKey)
    }

    val json = "{\n" +
            "   \"odata.metadata\" : \"https://134.65.16.195:50000/b1s/v1/\$metadata#Users/@Element\",\n" +
            "   \"InternalKey\" : 162,\n" +
            "   \"UserPassword\" : null,\n" +
            "   \"UserCode\" : \"ROVEMA\",\n" +
            "   \"UserName\" : \"implementação\",\n" +
            "   \"Superuser\" : \"tYES\",\n" +
            "   \"eMail\" : null,\n" +
            "   \"MobilePhoneNumber\" : null,\n" +
            "   \"Defaults\" : null,\n" +
            "   \"FaxNumber\" : null,\n" +
            "   \"Branch\" : -2,\n" +
            "   \"Department\" : -2,\n" +
            "   \"LanguageCode\" : \"ln_Portuguese_Br\",\n" +
            "   \"Locked\" : \"tNO\",\n" +
            "   \"Group\" : \"ug_Regular\",\n" +
            "   \"MaxDiscountGeneral\" : 100.0,\n" +
            "   \"MaxDiscountSales\" : 100.0,\n" +
            "   \"MaxDiscountPurchase\" : 100.0,\n" +
            "   \"CashLimit\" : \"tNO\",\n" +
            "   \"MaxCashAmtForIncmngPayts\" : 0.0,\n" +
            "   \"LastLogoutDate\" : \"2023-04-25\",\n" +
            "   \"LastLoginTime\" : \"14:27:37\",\n" +
            "   \"LastLogoutTime\" : \"14:22:04\",\n" +
            "   \"LastPasswordChangeTime\" : \"17:00:26\",\n" +
            "   \"LastPasswordChangedBy\" : \"FRANCISC\",\n" +
            "   \"UserPermission\" : [],\n" +
            "   \"UserActionRecord\" : [\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"Action\" : \"actionLogin\",\n" +
            "         \"ActionBy\" : \"ROVEMA\",\n" +
            "         \"ClientIP\" : \"10.117.163.225\",\n" +
            "         \"ClientName\" : \"SAP-ROVE3-INT\",\n" +
            "         \"ActionDate\" : \"2023-04-25\",\n" +
            "         \"ActionTime\" : \"14:27:37\",\n" +
            "         \"WindowsSession\" : 2,\n" +
            "         \"WindowsUser\" : \"partner\",\n" +
            "         \"ProcessName\" : \"SAP Business One.exe\",\n" +
            "         \"ProcessID\" : 14572,\n" +
            "         \"AliveDuration\" : 0\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"Action\" : \"actionLogoff\",\n" +
            "         \"ActionBy\" : \"ROVEMA\",\n" +
            "         \"ClientIP\" : \"10.117.163.225\",\n" +
            "         \"ClientName\" : \"SAP-ROVE3-INT\",\n" +
            "         \"ActionDate\" : \"2023-04-25\",\n" +
            "         \"ActionTime\" : \"14:22:04\",\n" +
            "         \"WindowsSession\" : 2,\n" +
            "         \"WindowsUser\" : \"partner\",\n" +
            "         \"ProcessName\" : \"SAP Business One.exe\",\n" +
            "         \"ProcessID\" : 652,\n" +
            "         \"AliveDuration\" : 0\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"Action\" : \"actionLoginFail\",\n" +
            "         \"ActionBy\" : \"ROVEMA\",\n" +
            "         \"ClientIP\" : \"10.117.163.225\",\n" +
            "         \"ClientName\" : \"10.117.163.225\",\n" +
            "         \"ActionDate\" : \"2023-04-24\",\n" +
            "         \"ActionTime\" : \"16:19:29\",\n" +
            "         \"WindowsSession\" : -1,\n" +
            "         \"WindowsUser\" : null,\n" +
            "         \"ProcessName\" : null,\n" +
            "         \"ProcessID\" : 0,\n" +
            "         \"AliveDuration\" : 0\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"Action\" : \"actionLogin\",\n" +
            "         \"ActionBy\" : \"ROVEMA\",\n" +
            "         \"ClientIP\" : \"10.117.163.225\",\n" +
            "         \"ClientName\" : \"SAP-ROVE3-INT\",\n" +
            "         \"ActionDate\" : \"2023-04-19\",\n" +
            "         \"ActionTime\" : \"16:18:16\",\n" +
            "         \"WindowsSession\" : 2,\n" +
            "         \"WindowsUser\" : \"partner\",\n" +
            "         \"ProcessName\" : \"SAP Business One.exe\",\n" +
            "         \"ProcessID\" : 652,\n" +
            "         \"AliveDuration\" : 8519\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"Action\" : \"actionLogoff\",\n" +
            "         \"ActionBy\" : \"ROVEMA\",\n" +
            "         \"ClientIP\" : \"10.117.163.225\",\n" +
            "         \"ClientName\" : \"SAP-ROVE3-INT\",\n" +
            "         \"ActionDate\" : \"2023-04-19\",\n" +
            "         \"ActionTime\" : \"16:10:07\",\n" +
            "         \"WindowsSession\" : 2,\n" +
            "         \"WindowsUser\" : \"partner\",\n" +
            "         \"ProcessName\" : \"SAP Business One.exe\",\n" +
            "         \"ProcessID\" : 652,\n" +
            "         \"AliveDuration\" : 0\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"Action\" : \"actionLogin\",\n" +
            "         \"ActionBy\" : \"ROVEMA\",\n" +
            "         \"ClientIP\" : \"10.117.163.225\",\n" +
            "         \"ClientName\" : \"SAP-ROVE3-INT\",\n" +
            "         \"ActionDate\" : \"2023-04-19\",\n" +
            "         \"ActionTime\" : \"16:06:01\",\n" +
            "         \"WindowsSession\" : 2,\n" +
            "         \"WindowsUser\" : \"partner\",\n" +
            "         \"ProcessName\" : \"SAP Business One.exe\",\n" +
            "         \"ProcessID\" : 652,\n" +
            "         \"AliveDuration\" : 0\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"Action\" : \"actionLoginFail\",\n" +
            "         \"ActionBy\" : \"ROVEMA\",\n" +
            "         \"ClientIP\" : \"10.117.163.225\",\n" +
            "         \"ClientName\" : \"10.117.163.225\",\n" +
            "         \"ActionDate\" : \"2023-04-19\",\n" +
            "         \"ActionTime\" : \"16:04:35\",\n" +
            "         \"WindowsSession\" : -1,\n" +
            "         \"WindowsUser\" : null,\n" +
            "         \"ProcessName\" : null,\n" +
            "         \"ProcessID\" : 0,\n" +
            "         \"AliveDuration\" : 0\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"Action\" : \"actionLogoff\",\n" +
            "         \"ActionBy\" : \"ROVEMA\",\n" +
            "         \"ClientIP\" : \"10.117.163.225\",\n" +
            "         \"ClientName\" : \"SAP-ROVE3-INT\",\n" +
            "         \"ActionDate\" : \"2023-04-18\",\n" +
            "         \"ActionTime\" : \"18:10:54\",\n" +
            "         \"WindowsSession\" : 2,\n" +
            "         \"WindowsUser\" : \"partner\",\n" +
            "         \"ProcessName\" : \"SAP Business One.exe\",\n" +
            "         \"ProcessID\" : 9816,\n" +
            "         \"AliveDuration\" : 0\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"Action\" : \"actionLogin\",\n" +
            "         \"ActionBy\" : \"ROVEMA\",\n" +
            "         \"ClientIP\" : \"10.117.163.225\",\n" +
            "         \"ClientName\" : \"SAP-ROVE3-INT\",\n" +
            "         \"ActionDate\" : \"2023-04-18\",\n" +
            "         \"ActionTime\" : \"17:06:20\",\n" +
            "         \"WindowsSession\" : 2,\n" +
            "         \"WindowsUser\" : \"partner\",\n" +
            "         \"ProcessName\" : \"SAP Business One.exe\",\n" +
            "         \"ProcessID\" : 9816,\n" +
            "         \"AliveDuration\" : 61\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"Action\" : \"actionLoginFail\",\n" +
            "         \"ActionBy\" : \"ROVEMA\",\n" +
            "         \"ClientIP\" : \"10.117.163.230\",\n" +
            "         \"ClientName\" : \"10.117.163.230\",\n" +
            "         \"ActionDate\" : \"2023-04-18\",\n" +
            "         \"ActionTime\" : \"17:03:04\",\n" +
            "         \"WindowsSession\" : -1,\n" +
            "         \"WindowsUser\" : null,\n" +
            "         \"ProcessName\" : null,\n" +
            "         \"ProcessID\" : 0,\n" +
            "         \"AliveDuration\" : 0\n" +
            "      }\n" +
            "   ],\n" +
            "   \"UserGroupByUser\" : [\n" +
            "      {\n" +
            "         \"USERId\" : 162,\n" +
            "         \"GroupId\" : 8,\n" +
            "         \"StartDate\" : null,\n" +
            "         \"DueDate\" : null\n" +
            "      }\n" +
            "   ],\n" +
            "   \"UserBranchAssignment\" : [\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 2\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 3\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 4\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 5\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 6\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 7\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 8\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 9\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 10\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 11\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 12\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 13\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 14\n" +
            "      },\n" +
            "      {\n" +
            "         \"UserCode\" : \"ROVEMA\",\n" +
            "         \"BPLID\" : 15\n" +
            "      }\n" +
            "   ]\n" +
            "}\n"
}
