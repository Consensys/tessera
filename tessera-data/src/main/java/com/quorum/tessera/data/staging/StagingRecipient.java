package com.quorum.tessera.data.staging;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

@Embeddable
public class StagingRecipient implements Serializable {

    @Basic private String recBytes;

    public StagingRecipient() {}

    public StagingRecipient(final byte[] bytes) {
        this.recBytes = Base64.getEncoder().encodeToString(bytes);
    }

    public void setBytes(final byte[] bytes) {
        this.recBytes = Base64.getEncoder().encodeToString(bytes);
    }

    public byte[] getBytes() {
        return Base64.getDecoder().decode(recBytes);
    }

    public String getRecBytes() {
        return recBytes;
    }

    public void setRecBytes(String recBytes) {
        this.recBytes = recBytes;
    }

    @Override
    public boolean equals(final Object o) {
        return (o instanceof StagingRecipient) && Objects.equals(recBytes, ((StagingRecipient) o).recBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getBytes());
    }

    @Override
    public String toString() {
        return recBytes;
    }
}
