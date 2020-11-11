package com.quorum.tessera.openapi;

public class P2POperationsFilter extends TagOperationsFilter {

    @Override
    public String getTagFilter() {
        return "peer-to-peer";
    }
}
