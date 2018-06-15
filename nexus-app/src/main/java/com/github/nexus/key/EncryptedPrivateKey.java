package com.github.nexus.key;

import com.github.nexus.argon2.ArgonOptions;

import javax.json.Json;
import javax.json.JsonObject;

public class EncryptedPrivateKey {

    private final ArgonOptions aopts;

    private final String snonce;

    private final String asalt;

    private final String sbox;

    public EncryptedPrivateKey(final ArgonOptions aopts,
                               final String snonce,
                               final String asalt,
                               final String sbox) {
        this.aopts = aopts;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
    }

    public ArgonOptions getAopts() {
        return aopts;
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

    public static EncryptedPrivateKey from(final JsonObject json) {

        final JsonObject aopts = json.getJsonObject("aopts");

        return new EncryptedPrivateKey(
            new ArgonOptions(
                aopts.getString("variant"),
                aopts.getInt("iterations"),
                aopts.getInt("memory"),
                aopts.getInt("parallelism")
            ),
            json.getString("snonce"),
            json.getString("asalt"),
            json.getString("sbox")
        );

    }

    public static JsonObject to(final EncryptedPrivateKey epk) {

        return Json.createObjectBuilder()
            .add("snonce", epk.getSnonce())
            .add("sbox", epk.getSbox())
            .add("asalt", epk.getAsalt())
            .add("aopts", Json.createObjectBuilder()
                .add("variant", epk.getAopts().getAlgorithm())
                .add("iterations", epk.getAopts().getIterations())
                .add("memory", epk.getAopts().getMemory())
                .add("parallelism", epk.getAopts().getParallelism())
                .build()
            ).build();

    }

}
