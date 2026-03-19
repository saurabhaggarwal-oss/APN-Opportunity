package com.ttn.ck.queryprocessor.exception;

public class RowIndexMandatory extends RuntimeException {
    public RowIndexMandatory() {
        super("Row Index is mandatory, Please use jsonProperty index on field value.");
    }
}
