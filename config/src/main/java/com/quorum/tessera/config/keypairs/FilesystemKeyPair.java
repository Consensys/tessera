package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.IOCallback;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;
import static java.nio.charset.StandardCharsets.UTF_8;

public class FilesystemKeyPair implements ConfigKeyPair {

    private final Path publicKeyPath;

    private final Path privateKeyPath;

    private String password = "";

    public FilesystemKeyPair(final Path publicKeyPath, final Path privateKeyPath) {
        this.publicKeyPath = publicKeyPath;
        this.privateKeyPath = privateKeyPath;
    }

    @Override
    public KeyData marshal() {
        return new KeyData(null, null, null, this.privateKeyPath, this.publicKeyPath);
    }

    @Override
    public String getPublicKey() {
        return IOCallback.execute(() -> new String(Files.readAllBytes(this.publicKeyPath), UTF_8));
    }

    @Override
    public String getPrivateKey() {
        final KeyDataConfig privateKey = JaxbUtil.unmarshal(
            IOCallback.execute(() -> Files.newInputStream(privateKeyPath)),
            KeyDataConfig.class
        );
        final PrivateKeyData pkd = privateKey.getPrivateKeyData();

        if (privateKey.getType() == UNLOCKED) {
            return privateKey.getValue();
        } else {
            return KeyEncryptorFactory.create().decryptPrivateKey(pkd, password).toString();
        }
    }

    @Override
    public void withPassword(final String password) {
        this.password = password;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
