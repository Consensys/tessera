package com.github.nexus.config;

public class PrivateKey {

    //field called "bytes" as is expected in the file (during deserialisation)
    //the getter is called getValue() as it is the value of the key
    //both refer to the same thing
    private final String bytes;

    private final String password;

    private final PrivateKeyType type;

    private final String snonce;

    private final String asalt;

    private final String sbox;

    private final ArgonOptions argonOptions;

    public PrivateKey() {
        this(null, null, null, null, null, null, null);
    }

    public PrivateKey(final String bytes,
                      final String password,
                      final PrivateKeyType type,
                      final String snonce,
                      final String asalt,
                      final String sbox,
                      final ArgonOptions argonOptions) {
        this.bytes = bytes;
        this.password = password;
        this.type = type;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
        this.argonOptions = argonOptions;
    }

    public String getValue() {
        return this.bytes;
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
