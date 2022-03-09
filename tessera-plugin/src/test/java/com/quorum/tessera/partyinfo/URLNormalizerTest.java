package com.quorum.tessera.partyinfo;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class URLNormalizerTest {

  @Test
  public void testTrailingSlash() {

    final String url1 = "http://someurl:8080";
    final String url2 = "http://someurl:8080/";

    assertThat(URLNormalizer.create().normalize(url1))
        .isEqualTo(URLNormalizer.create().normalize(url2));
  }

  @Test
  public void testInvalidUrl() {
    final String invalid = "!@£%$^";

    Throwable ex = catchThrowable(() -> URLNormalizer.create().normalize(invalid));

    assertThat(ex).isExactlyInstanceOf(RuntimeException.class);
    assertThat(ex).hasMessage("Invalid URL " + invalid);
  }
}
