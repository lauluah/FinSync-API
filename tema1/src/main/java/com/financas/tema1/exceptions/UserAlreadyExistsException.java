package com.financas.tema1.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("Já existe um usuário cadastrado com o e-mail: " + email);
    }
}
