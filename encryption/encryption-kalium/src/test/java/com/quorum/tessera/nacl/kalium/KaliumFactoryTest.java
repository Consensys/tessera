package com.quorum.tessera.nacl.kalium;

import com.quorum.tessera.nacl.NaclFacade;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KaliumFactoryTest {

    private final KaliumFactory kaliumFactory = new KaliumFactory();

    @Test
    public void createInstance() {
        final NaclFacade result = this.kaliumFactory.create();

        assertThat(result).isNotNull().isExactlyInstanceOf(Kalium.class);
    }

}
