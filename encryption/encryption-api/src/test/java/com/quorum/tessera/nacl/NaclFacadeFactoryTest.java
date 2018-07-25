package com.quorum.tessera.nacl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class NaclFacadeFactoryTest {

    private NaclFacadeFactory facadeFactory;

    @Before
    public void onSetUp() {
        this.facadeFactory = NaclFacadeFactory.newFactory();

        assertThat(this.facadeFactory).isExactlyInstanceOf(MockNaclFacadeFactory.class);
    }

    @Test
    public void create() {
        final NaclFacade result = this.facadeFactory.create();

        assertThat(result).isNotNull().isSameAs(MockNaclFacade.INSTANCE);
    }

}
