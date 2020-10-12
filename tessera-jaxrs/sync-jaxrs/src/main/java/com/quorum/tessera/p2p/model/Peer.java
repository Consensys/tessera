package com.quorum.tessera.p2p.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class Peer {
    @Schema(description = "peer's server url")
    public String url;
}
