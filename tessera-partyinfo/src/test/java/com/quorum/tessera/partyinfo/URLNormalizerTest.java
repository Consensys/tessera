package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.URLNormalizer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class URLNormalizerTest {

    @Test
    public void testTrailingSlash() {

        final String url1 = "http://someurl:8080";
        final String url2 = "http://someurl:8080/";

        assertThat(URLNormalizer.create().normalize(url1)).isEqualTo(URLNormalizer.create().normalize(url2));
    }

    @Test
    public void testInvalidUrl() {
        final String invalid = "!@£%$^";
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(
                        () -> {
                            URLNormalizer.create().normalize(invalid);
                            failBecauseExceptionWasNotThrown(Exception.class);
                        });
    }
}
