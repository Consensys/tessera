package com.quorum.tessera.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiObjectTest {

    @Test
    public void nonEmptyConstructor() {

        assertThat(new SendResponse("Data")).isNotNull().extracting(SendResponse::getKey).isNotNull();

        assertThat(new StoreRawResponse("Data".getBytes()))
                .isNotNull()
                .extracting(StoreRawResponse::getKey)
                .isNotNull();
    }
}
