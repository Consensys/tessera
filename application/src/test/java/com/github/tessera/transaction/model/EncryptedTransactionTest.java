package com.github.tessera.transaction.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptedTransactionTest {

    @Test
    public void twoObjectWithSameIdAreEqual() {

        final Long id = 1L;

        final EncryptedTransaction first = new EncryptedTransaction();
        first.setId(id);

        final EncryptedTransaction second = new EncryptedTransaction();
        second.setId(id);

        assertThat(first).isEqualTo(second).isNotSameAs(second)
            .hasSameHashCodeAs(second);

    }

    @Test
    public void twoObjectWithDifferentIdAreNotEqual() {

        final EncryptedTransaction first = new EncryptedTransaction();
        first.setId(1L);

        final EncryptedTransaction second = new EncryptedTransaction();
        second.setId(2L);

        assertThat(first).isNotEqualTo(second);

    }

    @Test
    public void sameObjectIsEqual() {

        final EncryptedTransaction first = new EncryptedTransaction();
        first.setId(1L);

        assertThat(first).isEqualTo(first).isSameAs(first);

    }

    @Test
    public void nullObjectIsNotEqual() {

        final EncryptedTransaction first = new EncryptedTransaction();
        first.setId(1L);

        final EncryptedTransaction second = null;

        assertThat(first).isNotEqualTo(second);

    }

    @Test
    public void objectOfDifferentTypesAreNotEqual() {

        final EncryptedTransaction first = new EncryptedTransaction();
        first.setId(1L);

        final OtherType second = new OtherType();
        second.setId(1L);

        assertThat(first).isNotEqualTo(second);

    }

    private static class OtherType extends EncryptedTransaction {
    }
}
