package com.ttn.ck.queryprocessor.builder.integeration;

import com.ttn.ck.queryprocessor.builder.dto.FilterComponentDTO;
import com.ttn.ck.queryprocessor.builder.dto.FilterDTO;
import com.ttn.ck.queryprocessor.builder.dto.Relation;
import com.ttn.ck.queryprocessor.builder.model.Operator;
import com.ttn.ck.queryprocessor.builder.secure.SecureFilter;

import java.util.List;

public class FilterWrapperDTOMapper {
    private FilterWrapperDTOMapper() {
    }

    public static SecureFilter mapToSecureFilter(List<FilterComponentDTO> components) {
        SecureFilter.SecureBuilder builder = SecureFilter.secureBuilder();
        if (components == null || components.isEmpty()) {
            return builder.build();
        }
        for (int i = 0; i < components.size(); i++) {
            applyComponent(builder, components.get(i), i == 0, components.get(0).getRelation());
        }
        return builder.build();
    }

    private static void applyComponent(
            SecureFilter.SecureBuilder builder,
            FilterComponentDTO component,
            boolean isFirst,
            Relation parentRelation
    ) {
        if (component == null) return;

        // Apply AND/OR before this component, except for the first
        if (!isFirst) {
            if (parentRelation == Relation.AND) builder.and();
            else builder.or();
        }

        // If group
        if (component.getGroups() != null && !component.getGroups().isEmpty()) {
            builder.open();
            for (int i = 0; i < component.getGroups().size(); i++) {
                applyComponent(builder, component.getGroups().get(i), i == 0, component.getRelation());
            }
            builder.close();
        }

        // If leaf
        else if (component.getFilter() != null) {
            FilterDTO f = component.getFilter();
            builder.apply(f.getColumn(), Operator.valueOf(f.getOperator()), f.getValue());
        }
    }
}
