package com.quorum.tessera.config.adapters;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.PrivateKeyType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PrivateKeyTypeAdapterTest {

  private PrivateKeyTypeAdapter adapter;

  public PrivateKeyTypeAdapterTest() {}

  @Before
  public void setUp() {
    adapter = new PrivateKeyTypeAdapter();
  }

  @After
  public void tearDown() {}

  @Test
  public void marshalLocked() {
    assertThat(adapter.marshal(PrivateKeyType.LOCKED)).isEqualTo("argon2sbox");
  }

  @Test
  public void marshalUnlocked() {
    assertThat(adapter.marshal(PrivateKeyType.UNLOCKED)).isEqualTo("unlocked");
  }

  @Test
  public void marshalNull() {
    assertThat(adapter.marshal(null)).isNull();
  }

  @Test
  public void unmarshalLocked() {
    assertThat(adapter.unmarshal("argon2sbox")).isEqualTo(PrivateKeyType.LOCKED);
  }

  @Test
  public void unmarshalUnlocked() {
    assertThat(adapter.unmarshal("unlocked")).isEqualTo(PrivateKeyType.UNLOCKED);
  }

  @Test
  public void unmarshalNull() {
    assertThat(adapter.unmarshal(null)).isNull();
  }
}
