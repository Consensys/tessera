package com.quorum.tessera.config.apps;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class TesseraAppFactoryTest {

    @Test
    public void createExisting() {
        Set<TesseraApp> result = TesseraAppFactory.create(CommunicationType.REST, AppType.P2P);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next()).isExactlyInstanceOf(MockTesseraApp.class);
    }

    @Test
    public void createOtherExisting() {
        Set<TesseraApp> result = TesseraAppFactory.create(CommunicationType.WEB_SOCKET, AppType.THIRD_PARTY);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next()).isExactlyInstanceOf(OtherMockTesseraApp.class);
    }

    @Test
    public void createNonExisting() {
        Set<TesseraApp> result = TesseraAppFactory.create(CommunicationType.WEB_SOCKET, AppType.P2P);

        assertThat(result).isEmpty();
    }
}
