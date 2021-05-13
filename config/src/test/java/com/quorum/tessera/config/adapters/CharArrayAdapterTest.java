package com.quorum.tessera.config.adapters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CharArrayAdapterTest {

  private CharArrayAdapter charArrayAdapter;

  public CharArrayAdapterTest() {}

  @Before
  public void setUp() {
    charArrayAdapter = new CharArrayAdapter();
  }

  @After
  public void tearDown() {
    charArrayAdapter = null;
  }

  @Test
  public void marshal() {
    char[] chars = new char[] {'s', 'o', 'm', 'e'};
    assertThat(charArrayAdapter.marshal(chars)).isEqualTo("some");
  }

  @Test
  public void marshalNull() {
    assertThat(charArrayAdapter.marshal(null)).isNull();
  }

  @Test
  public void unmarshal() {
    String s = "some";
    assertThat(charArrayAdapter.unmarshal(s)).isEqualTo("some".toCharArray());
  }

  @Test
  public void unmarshalNull() {
    assertThat(charArrayAdapter.unmarshal(null)).isNull();
  }
}
