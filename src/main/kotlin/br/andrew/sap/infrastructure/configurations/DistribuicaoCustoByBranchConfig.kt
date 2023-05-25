package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.model.WarehouseDefault
import br.andrew.sap.model.documents.DistribuicaoCustoByBranch

class DistribuicaoCustoByBranchConfig {

    companion object{
        var distibucoesCustos : List<DistribuicaoCustoByBranch> = listOf(
                DistribuicaoCustoByBranch("2","500","50000201"),
                DistribuicaoCustoByBranch("4","501","50100401"),
                DistribuicaoCustoByBranch("11","502","50200501")
        )
    }
}
