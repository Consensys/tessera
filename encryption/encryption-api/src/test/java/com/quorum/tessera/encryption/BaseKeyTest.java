package com.quorum.tessera.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class BaseKeyTest {

  @Test
  public void toStringDoesNotUseUnderlyingData() {
    final BaseKey k = new TestBaseKeyImpl(new byte[] {5, 6, 7});
    final BaseKey k2 = new TestBaseKeyImpl(new byte[] {5, 6, 7});

    assertThat(k.toString()).isNotEqualTo(k2.toString());
  }

  static class TestBaseKeyImpl extends BaseKey implements MyKey {

    protected TestBaseKeyImpl(byte[] keyBytes) {
      super(keyBytes);
    }
  }

  interface MyKey {}
}
