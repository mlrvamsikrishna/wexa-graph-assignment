package com.wexa.graph.web;

public class DatabaseUnavailableException extends RuntimeException {
    public DatabaseUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

