package com.financas.tema1.exceptions;

public class AiInsightException extends RuntimeException {
    public AiInsightException(String message) {
        super(message);
    }

    public AiInsightException(String message, Throwable cause) {
        super(message, cause);
    }
}
