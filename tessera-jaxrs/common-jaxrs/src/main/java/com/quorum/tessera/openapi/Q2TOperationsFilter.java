package com.quorum.tessera.openapi;

/**
 * Sample filter to avoid all resources for the /user resource
 **/
public class Q2TOperationsFilter extends TagOperationsFilter {

    @Override
    public String getTagFilter() {
        return "quorum-to-tessera";
    }
}
