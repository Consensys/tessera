package com.quorum.tessera.base64;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import org.junit.Test;

public class Base64CodecTest {

  public Base64CodecTest() {}

  @Test(expected = DecodingException.class)
  public void invalidBase64DataThrowsDecodeException() {
    Base64Codec.create().decode("1");
  }

  @Test
  public void decode() {

    byte[] result = Base64Codec.create().decode("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");

    assertThat(result).isNotEmpty();
  }

  @Test
  public void encodeToString() {

    byte[] data = "BOGUS".getBytes();

    String expected = Base64.getEncoder().encodeToString(data);

    String result = Base64Codec.create().encodeToString(data);
    assertThat(result).isEqualTo(expected);
  }
}
