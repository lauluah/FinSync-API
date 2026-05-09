package com.financas.tema1.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("Usuário não encontrado: " + email);
    }

    public UserNotFoundException(Long id) {
        super("Usuário não encontrado com id: " + id);
    }
}
