package com.github.nexus.transaction.model;

import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptedTransactionTest {

    @Test
    public void differentClassesNotEqual() {

        final Object other = "OTHER";
        final EncryptedTransaction et = new EncryptedTransaction();

        final boolean equal = Objects.equals(et, other);

        assertThat(equal).isFalse();

    }

}
