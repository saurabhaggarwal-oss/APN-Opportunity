package com.ttn.ck.queryprocessor.function;

@FunctionalInterface
public interface RowMapper<T, R> {
    T get(R r);
}
