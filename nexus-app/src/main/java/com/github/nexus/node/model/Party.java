package com.github.nexus.node.model;

import java.util.Objects;

public class Party {

    private final String url;

    public Party(final String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        return (o instanceof Party) && Objects.equals(url, ((Party) o).url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

}
