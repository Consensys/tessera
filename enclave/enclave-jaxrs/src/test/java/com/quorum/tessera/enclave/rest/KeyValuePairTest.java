package com.quorum.tessera.enclave.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KeyValuePairTest {

  @Test
  public void testParameterlessConstructor() {
    KeyValuePair keyValuePair = new KeyValuePair();
    assertThat(keyValuePair.getKey()).isNull();
    assertThat(keyValuePair.getValue()).isNull();
    keyValuePair.setKey("key".getBytes());
    keyValuePair.setValue("value".getBytes());
    assertThat(keyValuePair.getKey()).isNotNull();
    assertThat(keyValuePair.getValue()).isNotNull();
  }

  @Test
  public void testConstructor() {
    KeyValuePair keyValuePair = new KeyValuePair("key".getBytes(), "value".getBytes());
    assertThat(keyValuePair.getKey()).isNotNull();
    assertThat(keyValuePair.getValue()).isNotNull();
  }
}
