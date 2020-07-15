package com.quorum.tessera.version;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class VersionInfoTest {

    @Test
    public void create() {

        VersionInfo versionInfo = VersionInfo.create();
        assertThat(versionInfo).isNotNull();
        //Call for coverage cant access manifest.mf in test
        assertThat(versionInfo.currentVersion()).isNull();
        assertThat(versionInfo.previousVersion()).isNull();
    }

}
