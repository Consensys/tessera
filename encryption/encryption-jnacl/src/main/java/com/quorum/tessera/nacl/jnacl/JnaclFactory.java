package com.quorum.tessera.nacl.jnacl;

import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.NaclFacadeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import java.util.Map;

/**
 * Provides the JNaCL implementation of the {@link NaclFacade}
 */
public class JnaclFactory implements NaclFacadeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JnaclFactory.class);

    @Override
    public Encryptor create(Map<String, String> properties) {
        LOGGER.debug("Creating a JNaCL implementation of NaclFacadeFactory");

        final SecureRandom secureRandom = new SecureRandom();
        final JnaclSecretBox secretBox = new JnaclSecretBox();

        return new Jnacl(secureRandom, secretBox);
    }

    @Override
    public String getType() {
        return "NACL";
    }
}