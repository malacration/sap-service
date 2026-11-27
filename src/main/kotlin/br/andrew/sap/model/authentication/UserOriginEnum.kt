package br.andrew.sap.model.authentication

enum class UserOriginEnum {
    EmployeesInfo,
    SalePerson,
    BusinessPartner,
    /** Service account de integracao (Keycloak client_credentials): sem colaborador, vendedor ou filial. */
    Integracao
}