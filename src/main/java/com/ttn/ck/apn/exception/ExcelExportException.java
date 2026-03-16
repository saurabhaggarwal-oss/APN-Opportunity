package com.ttn.ck.apn.exception;

/**
 * Thrown when Excel file generation fails due to I/O or POI errors.
 */
public class ExcelExportException extends RuntimeException {

    public ExcelExportException(String message) {
        super(message);
    }

    public ExcelExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
