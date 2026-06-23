package com.novacasino.application.auth;

/** Output port for password hashing, decoupling use cases from the Spring Security encoder. */
public interface PasswordHasherPort {

    /** Hashes a raw password for storage. */
    String hash(String rawPassword);
}
