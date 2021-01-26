package com.quorum.tessera.enclave;

import java.util.*;
import java.util.stream.Stream;

public interface PrivacyGroupId {

    byte[] getBytes();

    String getBase64();

    static PrivacyGroupId from(final byte[] data) {

        return new PrivacyGroupId() {

            @Override
            public byte[] getBytes() {
                return data;
            }

            @Override
            public String getBase64() {
                return Base64.getEncoder().encodeToString(data);
            }

            @Override
            public boolean equals(Object arg0) {
                return getClass().isInstance(arg0) && Arrays.equals(data, getClass().cast(arg0).getBytes());
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(data);
            }

            @Override
            public String toString() {

                final String typeName =
                        Stream.of(getClass())
                                .map(Class::getInterfaces)
                                .flatMap(Stream::of)
                                .map(Class::getSimpleName)
                                .findFirst()
                                .get();

                return String.format("%s[%s]", typeName, getBase64());
            }
        };
    }

    static PrivacyGroupId from(final String base64Data) {
        return from(Base64.getDecoder().decode(base64Data));
    }
}
