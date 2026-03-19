package com.ttn.ck.queryprocessor.builder.integeration;

import com.ttn.ck.queryprocessor.builder.dto.HavingComponentDTO;
import com.ttn.ck.queryprocessor.builder.dto.HavingDTO;
import com.ttn.ck.queryprocessor.builder.dto.Relation;
import com.ttn.ck.queryprocessor.builder.model.Having;

import java.util.List;
import java.util.Locale;

public class HavingWrapperDTOMapper {

    private HavingWrapperDTOMapper() {
    }

    public static Having mapToHaving(List<HavingComponentDTO> components, List<Object> params) {
        Having.HavingBuilder builder = Having.builder();
        if (components == null || components.isEmpty()) {
            return builder.build();
        }
        for (int i = 0; i < components.size(); i++) {
            applyComponent(builder, params, components.get(i), i == 0, components.get(0).getRelation());
        }
        return builder.build();
    }

    private static void applyComponent(
            Having.HavingBuilder builder,
            List<Object> params,
            HavingComponentDTO component,
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
            builder.openBracket();
            for (int i = 0; i < component.getGroups().size(); i++) {
                applyComponent(builder, params, component.getGroups().get(i), i == 0, component.getRelation());
            }
            builder.closeBracket();
        }

        // If leaf
        else if (component.getHavingDTO() != null) {
            HavingDTO h = component.getHavingDTO();
            String fn = h.getFunction().toUpperCase(Locale.ROOT);
            String opSql = toSqlOperator(h.getOperator(), h.isNot());
            // Build expression like: SUM(col) >= ?
            builder.apply(fn, h.getColumn(), opSql);
            params.add(h.getValue());
        }
    }

    private static String toSqlOperator(String op, boolean not) {
        return switch (op.toUpperCase(Locale.ROOT)) {
            case "EQUALS" -> not ? "!=" : "=";
            case "NOT_EQUALS" -> "!=";
            case "GREATER_THAN" -> ">";
            case "GREATER_THAN_OR_EQUAL" -> ">=";
            case "LESS_THAN" -> "<";
            case "LESS_THAN_OR_EQUAL" -> "<=";
            default -> throw new IllegalArgumentException("Unsupported HAVING operator: " + op);
        };
    }
}
