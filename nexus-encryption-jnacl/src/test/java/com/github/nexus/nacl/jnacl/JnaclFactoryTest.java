package com.github.nexus.nacl.jnacl;

import com.github.nexus.nacl.NaclFacade;
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
