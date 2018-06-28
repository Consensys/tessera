package com.github.nexus.configuration.model;

import javax.json.JsonObject;
import java.util.Objects;

public class KeyData {

    private final String publicKey;

    private final JsonObject privateKey;

    private final String password;

    public KeyData(final String publicKey, final JsonObject privateKey, final String password) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.password = password;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public JsonObject getPrivateKey() {
        return privateKey;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof KeyData)) {
            return false;
        }

        final KeyData keyData = (KeyData) o;
        return Objects.equals(publicKey, keyData.publicKey) &&
            Objects.equals(privateKey, keyData.privateKey) &&
            Objects.equals(password, keyData.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicKey, privateKey, password);
    }

}
