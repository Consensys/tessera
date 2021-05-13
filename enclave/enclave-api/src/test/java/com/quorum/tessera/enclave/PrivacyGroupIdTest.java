package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.Test;

public class PrivacyGroupIdTest {

  @Test
  public void create() {
    PrivacyGroup.Id id = PrivacyGroup.Id.fromBytes("foo".getBytes());
    assertThat(id).isNotNull();
    assertThat(id.getBytes()).isEqualTo("foo".getBytes());
    assertThat(id.getBase64()).isEqualTo("Zm9v");
  }

  @Test
  public void createFromBase64Str() {
    PrivacyGroup.Id id = PrivacyGroup.Id.fromBase64String("Zm9v");
    assertThat(id).isNotNull();
    assertThat(id.getBytes()).isEqualTo("foo".getBytes());
    assertThat(id.getBase64()).isEqualTo("Zm9v");
  }

  @Test
  public void testEquals() {
    PrivacyGroup.Id id = PrivacyGroup.Id.fromBytes("foo".getBytes());
    PrivacyGroup.Id same = PrivacyGroup.Id.fromBase64String("Zm9v");
    assertThat(id).isEqualTo(same);

    PrivacyGroup.Id bogus = PrivacyGroup.Id.fromBytes("another".getBytes());
    assertThat(id).isNotEqualTo(bogus);

    assertThat(id.toString()).isEqualTo(PrivacyGroup.Id.class.getSimpleName() + "[Zm9v]");

    assertThat(id.hashCode()).isEqualTo(Arrays.hashCode("foo".getBytes()));
  }
}
