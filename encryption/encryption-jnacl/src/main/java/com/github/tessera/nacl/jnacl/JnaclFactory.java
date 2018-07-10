package com.github.tessera.nacl.jnacl;

import com.github.tessera.nacl.NaclFacade;
import com.github.tessera.nacl.NaclFacadeFactory;

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
