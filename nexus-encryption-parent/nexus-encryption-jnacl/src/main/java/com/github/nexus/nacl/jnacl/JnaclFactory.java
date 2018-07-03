package com.github.nexus.nacl.jnacl;

import com.github.nexus.nacl.NaclFacade;
import com.github.nexus.nacl.NaclFacadeFactory;

import java.security.SecureRandom;

/**
 * Provides the JNaCL implementation of the {@link NaclFacade}
 */
public class JnaclFactory implements NaclFacadeFactory {

    @Override
    public NaclFacade create() {
        final SecureRandom secureRandom = new SecureRandom();
        final JnaclSecretBox secretBox = new JnaclSecretBox();

        return new Jnacl(secureRandom, secretBox);
    }

}
