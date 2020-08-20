package com.github.foreignkey.exception;


/**
 * Description
 * SQL语句分析异常
 * <p>
 *
 * @author madokast
 * @version 1.0
 */

public class SQLParseException extends RuntimeException{

    public SQLParseException(String message) {
        super(message);
    }

    public SQLParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
