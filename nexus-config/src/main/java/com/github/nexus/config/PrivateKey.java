package com.github.nexus.config;

public class PrivateKey {

    private final String value;

    private final String password;

    private final PrivateKeyType type;

    private final String snonce;

    private final String asalt;

    private final String sbox;

    private final ArgonOptions argonOptions;

    public PrivateKey(
                      String value,
                      String password,
                      PrivateKeyType type,
                      String snonce,
                      String asalt,
                      String sbox,
                      ArgonOptions argonOptions) {
        this.value = value;
        this.password = password;
        this.type = type;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
        this.argonOptions = argonOptions;
    }

    public String getValue() {
        return this.value;
    }

    public String getPassword() {
        return password;
    }

    public PrivateKeyType getType() {
        return type;
    }

    public String getSnonce() {
        return snonce;
    }

    public String getAsalt() {
        return asalt;
    }

    public String getSbox() {
        return sbox;
    }

    public ArgonOptions getArgonOptions() {
        return argonOptions;
    }

}
