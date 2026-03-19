package com.ttn.ck.queryprocessor.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ttn.ck.queryprocessor.utils.ApplicationConstants.*;

@Data
@AllArgsConstructor
public class Filter {

    private StringBuilder stringBuilder;

    public static FilterBuilder builder() {
        return new FilterBuilder();
    }

    public static class FilterBuilder {
        private final StringBuilder stringBuilder = new StringBuilder();
        public FilterBuilder col(String key) {
            this.stringBuilder.append(key);
            return this;
        }

        public FilterBuilder eq(Integer value) {
            this.stringBuilder.append(EQUAL).append(value.toString()).append(SPACE);
            return this;
        }

        public FilterBuilder eq(String value) {
            this.stringBuilder.append(EQUAL).append(SINGLE_QUOTE).append(value).append(SINGLE_QUOTE).append(SPACE);
            return this;
        }

        public FilterBuilder eqDynamicValue(String value) {
            this.stringBuilder.append(EQUAL).append(value).append(SPACE);
            return this;
        }

        public FilterBuilder and() {
            this.stringBuilder.append(AND);
            return this;
        }

        public FilterBuilder and(StringBuilder filter1, StringBuilder filter2) {
            this.stringBuilder.append(OPEN_BRACKET).append(filter1).append(AND).append(filter2).append(CLOSE_BRACKET);
            return this;
        }

        public FilterBuilder or(StringBuilder filter1, StringBuilder filter2) {
            this.stringBuilder.append(OPEN_BRACKET).append(filter1).append(OR).append(filter2).append(CLOSE_BRACKET);
            return this;
        }

        public FilterBuilder or() {
            this.stringBuilder.append(OR);
            return this;
        }

        public FilterBuilder in(Iterable<String> values) {
            List<String> valuesList = new ArrayList<>();
            for (String value : values) {
                valuesList.add(SINGLE_QUOTE + value + SINGLE_QUOTE);
            }
            this.stringBuilder.append(IN).append(OPEN_BRACKET).append(String.join(COMMA_DELIMETER, valuesList)).append(CLOSE_BRACKET);
            return this;
        }

        public FilterBuilder notIn(Iterable<String> values) {
            List<String> valuesList = new ArrayList<>();
            for (String value : values) {
                valuesList.add(SINGLE_QUOTE + value + SINGLE_QUOTE);
            }
            this.stringBuilder.append(NOT_IN).append(OPEN_BRACKET).append(String.join(COMMA_DELIMETER, valuesList)).append(CLOSE_BRACKET);
            return this;
        }

        public FilterBuilder between(Object value1, Object value2) {
            this.stringBuilder.append(BETWEEN).append(SINGLE_QUOTE).append(value1).append(SINGLE_QUOTE).append(SPACE).append(AND)
                    .append(SINGLE_QUOTE).append(value2).append(SINGLE_QUOTE).append(SPACE);
            return this;
        }

        public FilterBuilder appendFilter(StringBuilder filter) {
            this.stringBuilder.append(filter);
            return this;
        }

        public FilterBuilder and(StringBuilder filter) {
            if (Objects.nonNull(filter)){
                this.stringBuilder.append(AND).append(filter);
            }
            return this;
        }

        public StringBuilder build() {
            return this.stringBuilder;
        }
    }

}
