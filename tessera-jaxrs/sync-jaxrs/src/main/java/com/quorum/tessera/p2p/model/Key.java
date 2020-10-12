package com.quorum.tessera.p2p.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class Key {
    @Schema(description = "known public key of peer", format = "base64")
    public String key;

    @Schema(description = "public key's corresponding peer url")
    public String url;
}
