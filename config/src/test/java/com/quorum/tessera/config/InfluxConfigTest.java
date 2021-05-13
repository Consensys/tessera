package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.net.URI;
import org.junit.Test;

public class InfluxConfigTest {

  @Test
  public void isSslFalseIfNoSslConfig() {
    InfluxConfig influxConfig = new InfluxConfig();
    assertThat(influxConfig.isSsl()).isFalse();
  }

  @Test
  public void isSslFalseIfSslAuthenticationModeIsOff() {
    SslConfig sslConfig = new SslConfig();
    sslConfig.setTls(SslAuthenticationMode.OFF);

    InfluxConfig influxConfig = new InfluxConfig();
    influxConfig.setSslConfig(sslConfig);

    assertThat(influxConfig.isSsl()).isFalse();
  }

  @Test
  public void isSslTrueIfSslAuthenticationModeIsStrict() {
    SslConfig sslConfig = new SslConfig();
    sslConfig.setTls(SslAuthenticationMode.STRICT);

    InfluxConfig influxConfig = new InfluxConfig();
    influxConfig.setSslConfig(sslConfig);

    assertThat(influxConfig.isSsl()).isTrue();
  }

  @Test
  public void getServerUriCreatesUriFromStringAddress() {
    String address = "http://address";

    InfluxConfig influxConfig = new InfluxConfig();
    influxConfig.setServerAddress(address);

    URI result = influxConfig.getServerUri();
    URI expected = URI.create(address);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void getServerUriThrowsConfigExceptionIfInvalidUriSyntax() {
    String address = "http://space not allowed in uri";

    InfluxConfig influxConfig = new InfluxConfig();
    influxConfig.setServerAddress(address);

    Throwable ex = catchThrowable(() -> influxConfig.getServerUri());

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
  }
}
