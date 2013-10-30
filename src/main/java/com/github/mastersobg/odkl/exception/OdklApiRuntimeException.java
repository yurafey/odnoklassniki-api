package com.github.mastersobg.odkl.exception;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class OdklApiRuntimeException extends RuntimeException {

    public OdklApiRuntimeException(String cause) {
        super(cause);
    }

    public OdklApiRuntimeException() {}

    public OdklApiRuntimeException(Throwable cause) {
        super(cause);
    }
}
