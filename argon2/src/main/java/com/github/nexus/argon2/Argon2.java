package com.github.nexus.argon2;

import java.util.ServiceLoader;

public interface Argon2 {

    ArgonResult hash(ArgonOptions options, String password, byte[] salt);

    ArgonResult hash(String password, byte[] salt);

    static Argon2 create() {
        return ServiceLoader.load(Argon2.class).iterator().next();
    }

}
