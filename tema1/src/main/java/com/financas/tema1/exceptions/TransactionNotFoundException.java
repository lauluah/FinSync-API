package com.financas.tema1.exceptions;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(Long id) {
        super("Transação não encontrada com id: " + id);
    }
}
