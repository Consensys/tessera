package com.quorum.tessera.enclave;

import com.quorum.tessera.version.BaseVersion;
import com.quorum.tessera.version.CBORSupportVersion;
import java.util.Set;
import java.util.stream.Stream;

public enum EncodedPayloadCodec {
  CBOR(CBORSupportVersion.API_VERSION_5),
  LEGACY(BaseVersion.API_VERSION_1);

  String minimumSupportedVersion;

  EncodedPayloadCodec(String minimumSupportedVersion) {
    this.minimumSupportedVersion = minimumSupportedVersion;
  }

  public String getMinimumSupportedVersion() {
    return minimumSupportedVersion;
  }

  public static EncodedPayloadCodec current() {
    return CBOR;
  }

  public static EncodedPayloadCodec getPreferredCodec(Set<String> versions) {
    return Stream.of(EncodedPayloadCodec.values())
        .filter(codec -> versions.contains(codec.getMinimumSupportedVersion()))
        .findFirst()
        .orElse(LEGACY);
  }
}
