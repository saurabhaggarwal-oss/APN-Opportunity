package com.ttn.ck.queryprocessor.builder.integeration;

import com.ttn.ck.queryprocessor.builder.dto.ObjectQueryDTO;
import com.ttn.ck.queryprocessor.builder.model.CTE;
import com.ttn.ck.queryprocessor.builder.secure.ObjectQueryDtoValidator;
import com.ttn.ck.queryprocessor.builder.secure.PreparedQuery;
import com.ttn.ck.queryprocessor.builder.secure.SqlValidationEngine;

import java.util.ArrayList;
import java.util.List;

public final class ObjectQueryAssembler {

    private final SqlValidationEngine ve;
    private static final QueryBuilderWrapper queryBuilderWrapper = new QueryBuilderWrapper();

    public ObjectQueryAssembler(SqlValidationEngine ve) {
        this.ve = ve;
    }

    public PreparedQuery build(ObjectQueryDTO dto) {
        new ObjectQueryDtoValidator(ve).validateObjectQueryDTO(dto);
        List<Object> params = new ArrayList<>();

        // 1) CTE
        CTE cte = new CTE();
        if (dto.getCtes() != null && !dto.getCtes().isEmpty()) {
            CTEWrapper cteWrapper = new CTEWrapper(dto.getCtes(), params, ve);
            cte = cteWrapper.toCTE();
        }
        QueryBuilderWrapper.QueryBuildResult mainResult = queryBuilderWrapper.build(dto.getQueryComponents(), cte, ve);
        String sql = mainResult.sql();
        params.addAll(mainResult.params());
        return new PreparedQuery(sql, params);
    }
}
