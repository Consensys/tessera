package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PrivacyGroupRetrieveRequest {

    @Schema(description = "id identifying the privacy group to retrieve", type = "string", format = "base64")
    @NotNull
    @Size(min = 1)
    private String privacyGroupId;

    public String getPrivacyGroupId() {
        return privacyGroupId;
    }

    public void setPrivacyGroupId(String privacyGroupId) {
        this.privacyGroupId = privacyGroupId;
    }
}
