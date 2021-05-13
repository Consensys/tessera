package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JaxbCreateFactoryTest {

  private final Class type;

  public JaxbCreateFactoryTest(final Class type) {
    this.type = type;
  }

  private final OtherType otherType = new OtherType();

  static class OtherType {}

  @Parameterized.Parameters
  public static List<Class> params() {
    return Arrays.asList(
        ArgonOptions.class,
        Config.class,
        KeyData.class,
        Peer.class,
        SslConfig.class,
        JdbcConfig.class,
        KeyDataConfig.class,
        PrivateKeyData.class,
        AzureKeyVaultConfig.class);
  }

  @Test
  public void createDefault() throws Exception {

    final Object instance = type.newInstance();

    assertThat(instance).isNotNull();

    assertThat(instance).isEqualTo(instance);
    assertThat(instance.hashCode()).isEqualTo(instance.hashCode());
    assertThat(instance).isNotEqualTo(otherType);
    assertThat(instance).isNotEqualTo(null);
  }
}
