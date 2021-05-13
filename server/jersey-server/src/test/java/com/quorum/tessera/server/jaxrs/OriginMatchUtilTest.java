package com.quorum.tessera.server.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class OriginMatchUtilTest {

  @Test
  public void matchWildCard() {

    List<String> tokens = Arrays.asList("*");
    String origin = "http://bogus.com";
    OriginMatchUtil matcher = new OriginMatchUtil(tokens);

    assertThat(matcher.matches(origin)).isTrue();
  }

  @Test
  public void exactMatch() {
    List<String> tokens = Arrays.asList("http://bogus.com");
    String origin = "http://bogus.com";
    OriginMatchUtil matcher = new OriginMatchUtil(tokens);

    assertThat(matcher.matches(origin)).isTrue();
  }

  @Test
  public void withSubDomain() {
    List<String> tokens = Arrays.asList("http://*.bogus.com");
    String origin = "http://myhost.bogus.com";

    OriginMatchUtil matcher = new OriginMatchUtil(tokens);
    assertThat(matcher.matches(origin)).isTrue();
  }

  @Test
  public void withBadSubDomain() {
    List<String> tokens = Arrays.asList("http://*.bogus.com");
    String origin = "http://myhost.other.com";

    OriginMatchUtil matcher = new OriginMatchUtil(tokens);
    assertThat(matcher.matches(origin)).isFalse();
  }

  @Test
  public void withNullOrigin() {
    List<String> tokens = Arrays.asList("http://*.bogus.com");
    OriginMatchUtil matcher = new OriginMatchUtil(tokens);
    assertThat(matcher.matches(null)).isFalse();
  }

  @Test
  public void withEmptyOrigin() {
    List<String> tokens = Arrays.asList("http://*.bogus.com");
    OriginMatchUtil matcher = new OriginMatchUtil(tokens);
    assertThat(matcher.matches("")).isFalse();
  }

  @Test
  public void withSubDomainWithPort() {
    List<String> tokens = Arrays.asList("http://*.bogus.com");
    String origin = "http://myhost.bogus.com:989";

    OriginMatchUtil matcher = new OriginMatchUtil(tokens);
    assertThat(matcher.matches(origin)).isFalse();
  }

  @Test
  public void withDifferentScheme() {
    List<String> tokens = Arrays.asList("https://bogus.com");
    String origin = "http://bogus.com";

    OriginMatchUtil matcher = new OriginMatchUtil(tokens);
    assertThat(matcher.matches(origin)).isFalse();
  }

  @Test
  public void withSamePorts() {
    List<String> tokens = Arrays.asList("http://bogus.com:989");
    String origin = "http://bogus.com:989";

    OriginMatchUtil matcher = new OriginMatchUtil(tokens);
    assertThat(matcher.matches(origin)).isTrue();
  }

  @Test
  public void withDifferentCases() {
    List<String> tokens = Arrays.asList("HTTP://BOGUS.cOm:989");
    String origin = "http://bogus.com:989";

    OriginMatchUtil matcher = new OriginMatchUtil(tokens);
    assertThat(matcher.matches(origin)).isTrue();
  }

  @Test
  public void withDifferentCases2() {
    List<String> tokens = Arrays.asList("http://bogus.com:989");
    String origin = "HTTP://BOGUS.cOm:989";

    OriginMatchUtil matcher = new OriginMatchUtil(tokens);
    assertThat(matcher.matches(origin)).isTrue();
  }
}
