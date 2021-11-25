package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.version.*;
import java.util.Set;
import org.junit.Test;

public class EncodedPayloadCodecTest {

  @Test
  public void current() {
    EncodedPayloadCodec encodedPayloadCodec = EncodedPayloadCodec.current();
    assertThat(encodedPayloadCodec).isSameAs(EncodedPayloadCodec.CBOR);
  }

  @Test
  public void getPreferredCodecLegacy() {
    EncodedPayloadCodec codec =
        EncodedPayloadCodec.getPreferredCodec(
            Set.of(BaseVersion.API_VERSION_1, MandatoryRecipientsVersion.API_VERSION_4));

    assertThat(codec).isEqualTo(EncodedPayloadCodec.LEGACY);
  }

  @Test
  public void getPreferredCodecVersion5() {
    EncodedPayloadCodec codec =
        EncodedPayloadCodec.getPreferredCodec(
            Set.of(
                BaseVersion.API_VERSION_1,
                MandatoryRecipientsVersion.API_VERSION_4,
                CBORSupportVersion.API_VERSION_5));

    assertThat(codec).isEqualTo(EncodedPayloadCodec.CBOR);
    assertThat(codec.getMinimumSupportedVersion()).isEqualTo(CBORSupportVersion.API_VERSION_5);
  }

  @Test
  public void getPreferredCodecUnknownVersion() {
    EncodedPayloadCodec codec = EncodedPayloadCodec.getPreferredCodec(Set.of());

    assertThat(codec).isEqualTo(EncodedPayloadCodec.LEGACY);
    assertThat(codec.getMinimumSupportedVersion()).isEqualTo(BaseVersion.API_VERSION_1);
  }
}
