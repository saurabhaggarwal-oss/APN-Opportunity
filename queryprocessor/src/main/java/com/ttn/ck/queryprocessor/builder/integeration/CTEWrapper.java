package com.ttn.ck.queryprocessor.builder.integeration;


import com.ttn.ck.queryprocessor.builder.dto.CteDTO;
import com.ttn.ck.queryprocessor.builder.dto.QueryComponentDTO;
import com.ttn.ck.queryprocessor.builder.model.CTE;
import com.ttn.ck.queryprocessor.builder.model.CTE.CTEDefinition;
import com.ttn.ck.queryprocessor.builder.secure.SqlValidationEngine;

import java.util.ArrayList;
import java.util.List;

public class CTEWrapper {
    private final List<CTEDefinition> definitions = new ArrayList<>();
    private static final QueryBuilderWrapper queryBuilderWrapper = new QueryBuilderWrapper();

    public CTEWrapper(List<CteDTO> ctes, List<Object> params, SqlValidationEngine ve) {
        if (ctes == null) return;
        for (CteDTO cte : ctes) {
            if (cte == null || cte.getName() == null || cte.getQueryComponentDTO() == null) continue;
            QueryComponentDTO qc = cte.getQueryComponentDTO();
            QueryBuilderWrapper.QueryBuildResult result = queryBuilderWrapper.build(qc, ve);
            String subQuery = result.sql();
            params.addAll(result.params());
            definitions.add(new CTEDefinition(cte.getName(), subQuery));
        }
    }

    public boolean isEmpty() {
        return definitions.isEmpty();
    }
    public CTE toCTE() { return new CTE(definitions); }

}
