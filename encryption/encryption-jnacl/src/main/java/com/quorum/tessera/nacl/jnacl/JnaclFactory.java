package com.quorum.tessera.nacl.jnacl;

import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.NaclFacadeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

/**
 * Provides the JNaCL implementation of the {@link NaclFacade}
 */
public class JnaclFactory implements NaclFacadeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JnaclFactory.class);

    @Override
    public NaclFacade create() {
        LOGGER.debug("Creating a JNaCL implementation of NaclFacadeFactory");

        final SecureRandom secureRandom = new SecureRandom();
        final JnaclSecretBox secretBox = new JnaclSecretBox();

        return new Jnacl(secureRandom, secretBox);
    }

}
