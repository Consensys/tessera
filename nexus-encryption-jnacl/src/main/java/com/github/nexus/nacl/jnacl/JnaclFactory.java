package com.github.nexus.nacl.jnacl;

import com.github.nexus.nacl.NaclFacade;
import com.github.nexus.nacl.NaclFacadeFactory;

import java.security.SecureRandom;

public class JnaclFactory implements NaclFacadeFactory {

    @Override
    public NaclFacade create() {
        final SecureRandom secureRandom = new SecureRandom();
        final JnaclSecretBox secretBox = new JnaclSecretBox();

        return new Jnacl(secureRandom, secretBox);
    }

}
