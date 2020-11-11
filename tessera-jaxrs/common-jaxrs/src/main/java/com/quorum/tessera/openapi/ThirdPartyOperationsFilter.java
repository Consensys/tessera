package com.quorum.tessera.openapi;

public class ThirdPartyOperationsFilter extends TagOperationsFilter {

    @Override
    public String getTagFilter() {
        return "third-party";
    }
}
