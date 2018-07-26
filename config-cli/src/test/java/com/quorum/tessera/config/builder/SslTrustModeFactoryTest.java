package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.SslTrustMode;
import java.util.EnumMap;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class SslTrustModeFactoryTest {

    @Test
    public void resolveSslTrustModeNone() {

        assertThat(SslTrustModeFactory.resolveByLegacyValue(null)).isEqualTo(SslTrustMode.NONE);
        assertThat(SslTrustModeFactory.resolveByLegacyValue("BOGUS")).isEqualTo(SslTrustMode.NONE);
    }

    @Test
    public void resolveSslTrustMode() {

        java.util.Map<SslTrustMode, String> fixtures = new EnumMap<>(SslTrustMode.class);

        fixtures.put(SslTrustMode.CA, "ca");
        fixtures.put(SslTrustMode.TOFU, "tofu");
        fixtures.put(SslTrustMode.CA_OR_TOFU, "ca-or-tofu");
        fixtures.put(SslTrustMode.NONE, "none");

        for (SslTrustMode mode : fixtures.keySet()) {
            SslTrustMode result = SslTrustModeFactory.resolveByLegacyValue(fixtures.get(mode));
            assertThat(result).isEqualTo(mode);
        }
    }

    @Test
    public void resolveSslTrustModeForCaOrTofu() {
        SslTrustMode result = SslTrustModeFactory.resolveByLegacyValue("ca-or-tofu");
        assertThat(result).isEqualTo(SslTrustMode.CA_OR_TOFU);

    }
}
