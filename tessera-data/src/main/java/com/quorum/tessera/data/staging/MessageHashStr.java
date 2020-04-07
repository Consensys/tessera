package com.quorum.tessera.data.staging;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Base64;
import java.util.Objects;

@Embeddable
public class MessageHashStr implements Serializable {

    @Basic private String hash;

    public MessageHashStr() {}

    public MessageHashStr(final byte[] hashBytes) {
        this.hash = Base64.getEncoder().encodeToString(hashBytes);
    }

    public void setHashBytes(final byte[] hashBytes) {
        this.hash = Base64.getEncoder().encodeToString(hashBytes);
    }

    public byte[] getHashBytes() {
        return Base64.getDecoder().decode(hash);
    }

    @Override
    public boolean equals(final Object o) {
        return (o instanceof MessageHashStr) && Objects.equals(hash, ((MessageHashStr) o).hash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hash);
    }

    @Override
    public String toString() {
        return hash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
