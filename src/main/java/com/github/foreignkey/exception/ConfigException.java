package com.github.foreignkey.exception;


/**
 * 配置异常
 *
 * @author madokast
 * @version 1.0
 */

public class ConfigException extends RuntimeException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
