package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidPath;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.IOCallback;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FilesystemKeyPair implements ConfigKeyPair {

    @NotNull
    @ValidPath(checkExists = true, message = "File does not exist")
    @XmlElement
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path publicKeyPath;

    @NotNull
    @ValidPath(checkExists = true, message = "File does not exist")
    @XmlElement
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path privateKeyPath;

    private InlineKeypair inlineKeypair;

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
        loadKeys();
        return this.inlineKeypair.getPublicKey();
    }

    @Override
    public String getPrivateKey() {
        loadKeys();
        return this.inlineKeypair.getPrivateKey();
    }

    @Override
    public void withPassword(final String password) {
        this.password = password;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    private void loadKeys() {
        if(inlineKeypair == null) {
            this.inlineKeypair = new InlineKeypair(
                IOCallback.execute(() -> new String(Files.readAllBytes(this.publicKeyPath), UTF_8)),
                JaxbUtil.unmarshal(
                    IOCallback.execute(() -> Files.newInputStream(privateKeyPath)),
                    KeyDataConfig.class
                )
            );
        }
        this.inlineKeypair.withPassword(this.password);
    }

    //For testing only
    //TODO: remove
    public InlineKeypair getInlineKeypair() {
        loadKeys();
        return inlineKeypair;
    }

}
