package com.quorum.tessera.nacl.jnacl;

import com.quorum.tessera.nacl.NaclFacade;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JnaclFactoryTest {

    private JnaclFactory jnaclFactory;

    @Before
    public void setUp() {
        this.jnaclFactory = new JnaclFactory();
    }

    @Test
    public void createInstance() {
        final NaclFacade result = jnaclFactory.create();

        assertThat(result).isNotNull().isExactlyInstanceOf(Jnacl.class);
    }

}
