package com.github.nexus.key;

import com.github.nexus.argon2.ArgonOptions;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * POJO encapsulating the structure of the "data" field in a private key
 * when it as been encrypted using Argon2/NaCL
 */
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

    /**
     * Reads a JSON Object that has the same structure as this class
     * convenience method for marshalling
     *
     * @param json the json object that is expected to have the same properties as this class
     * @return a populated object with the same properties as the input
     */
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

    /**
     * Writes to a JSON representation of this object, including nested objects
     *
     * @param epk the object to marshall
     * @return a {@link JsonObject} with the fields populated using values from the given object
     */
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
