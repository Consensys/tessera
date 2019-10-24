
package com.jpmorgan.quorum.encryption.aec;

import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;

public class AecEncryptorFactory implements EncryptorFactory {

    @Override
    public Encryptor create() {
        return new AecEncryptor("AES/GCM/NoPadding", "secp256r1",24,32);
    }    
}
