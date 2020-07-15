package com.quorum.tessera.test.rest;

import com.quorum.tessera.server.http.VersionHeaderDecorator;
import com.quorum.tessera.version.VersionInfo;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public interface AssertApiHeaders {

    VersionInfo VERSIONINFO = VersionInfo.create();

    static void doAsserts(Response response) {

        assertThat(VERSIONINFO.currentVersion()).isNotNull();
        assertThat(VERSIONINFO.previousVersion()).isNotNull();

        assertThat(response.getHeaders())
            .containsKeys(VersionHeaderDecorator.CURRENT_VERSION,VersionHeaderDecorator.PREVIOUS_VERSION);

        assertThat(response.getHeaders().get(VersionHeaderDecorator.CURRENT_VERSION)).hasSize(1)
            .containsExactly(VERSIONINFO.currentVersion());
        assertThat(response.getHeaders().get(VersionHeaderDecorator.PREVIOUS_VERSION))
            .hasSize(1).containsExactly(VERSIONINFO.previousVersion());
    }
}
