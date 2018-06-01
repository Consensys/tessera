package com.github.nexus.entity;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class SomeEntityTest {

    public SomeEntityTest() {
    }

    @Test
    public void twoObjectWithSameIdAreEqual() {

        final Long id = 1L;

        final SomeEntity first = new SomeEntity();
        first.setId(id);

        final SomeEntity second = new SomeEntity();
        second.setId(id);

        assertThat(first).isEqualTo(second).isNotSameAs(second)
                .hasSameHashCodeAs(second);

    }

    @Test
    public void twoObjectWithDifferentIdAreNotEqual() {

        final SomeEntity first = new SomeEntity();
        first.setId(1L);

        final SomeEntity second = new SomeEntity();
        second.setId(2L);

        assertThat(first).isNotEqualTo(second);

    }

    @Test
    public void sameObjectIsEqual() {

        final SomeEntity first = new SomeEntity();
        first.setId(1L);

        assertThat(first).isEqualTo(first).isSameAs(first);

    }

    @Test
    public void nullObjectIsNotEqual() {

        final SomeEntity first = new SomeEntity();
        first.setId(1L);

        final SomeEntity second = null;

        assertThat(first).isNotEqualTo(second);

    }
    
        @Test
    public void objectOfDifferentTypesAreNotEqual() {

        final SomeEntity first = new SomeEntity();
        first.setId(1L);

        final OtherType second = new OtherType();
        second.setId(1L);

        assertThat(first).isNotEqualTo(second);

    }
    
    static class OtherType extends SomeEntity {}
}
