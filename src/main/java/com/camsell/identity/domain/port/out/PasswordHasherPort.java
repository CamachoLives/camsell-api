package com.camsell.identity.domain.port.out;

/** Abstraccion del algoritmo de hash, para que el dominio no conozca BCrypt. */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
