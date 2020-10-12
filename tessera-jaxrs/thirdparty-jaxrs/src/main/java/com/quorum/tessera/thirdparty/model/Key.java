package com.quorum.tessera.thirdparty.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class Key {

    @Schema(description = "public key", format = "base64")
    public String key;
}
