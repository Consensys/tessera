package com.quorum.tessera.encryption;

import java.util.stream.Stream;

class PublicKeyImpl extends BaseKey implements PublicKey {

  PublicKeyImpl(byte[] keyBytes) {
    super(keyBytes);
  }

  @Override
  public final String toString() {

    final String typeName =
        Stream.of(getClass())
            .map(Class::getInterfaces)
            .flatMap(Stream::of)
            .map(Class::getSimpleName)
            .findFirst()
            .get();

    return String.format("%s[%s]", typeName, encodeToBase64());
  }
}
