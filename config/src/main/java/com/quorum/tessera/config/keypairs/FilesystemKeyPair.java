package com.quorum.tessera.config.keypairs;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidBase64;
import com.quorum.tessera.config.constraints.ValidContent;
import com.quorum.tessera.config.constraints.ValidPath;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.IOCallback;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilesystemKeyPair implements ConfigKeyPair {

  private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemKeyPair.class);

  @ValidContent(
      minLines = 1,
      maxLines = 1,
      message = "file expected to contain a single non empty value")
  @NotNull
  @ValidPath(checkExists = true, message = "File does not exist")
  @XmlElement
  @XmlJavaTypeAdapter(PathAdapter.class)
  private final Path publicKeyPath;

  @ValidContent(minLines = 1, message = "file expected to contain at least one line")
  @NotNull
  @ValidPath(checkExists = true, message = "File does not exist")
  @XmlElement
  @XmlJavaTypeAdapter(PathAdapter.class)
  private final Path privateKeyPath;

  private InlineKeypair inlineKeypair;

  private char[] password;

  private final KeyEncryptor keyEncryptor;

  //    public FilesystemKeyPair(final Path publicKeyPath, final Path privateKeyPath) {
  //        this(
  //                publicKeyPath,
  //                privateKeyPath,
  //                KeyEncryptorFactory.newFactory()
  //                        .create(
  //                                new EncryptorConfig() {
  //                                    {
  //                                        setType(EncryptorType.NACL);
  //                                    }
  //                                }));
  //    }

  public FilesystemKeyPair(
      final Path publicKeyPath, final Path privateKeyPath, final KeyEncryptor keyEncryptor) {
    this.publicKeyPath = publicKeyPath;
    this.privateKeyPath = privateKeyPath;
    this.keyEncryptor = keyEncryptor;

    try {
      loadKeys();
    } catch (final Exception ex) {
      // silently discard errors as these get picked up by the validator
      LOGGER.debug("Unable to read key files", ex);
    }
  }

  @Override
  @Size(min = 1)
  @ValidBase64(message = "Invalid Base64 key provided")
  public String getPublicKey() {
    if (this.inlineKeypair == null) {
      return null;
    }
    return this.inlineKeypair.getPublicKey();
  }

  @Override
  @Size(min = 1)
  @ValidBase64(message = "Invalid Base64 key provided")
  @Pattern(
      regexp = "^((?!NACL_FAILURE).)*$",
      message =
          "Could not decrypt the private key with the provided password, please double check the passwords provided")
  public String getPrivateKey() {
    if (this.inlineKeypair == null) {
      return null;
    }
    return this.inlineKeypair.getPrivateKey();
  }

  @Override
  public void withPassword(final char[] password) {
    this.password = password;
    if (this.inlineKeypair != null) {
      this.inlineKeypair.withPassword(this.password);
    }
  }

  @Override
  public char[] getPassword() {
    return this.password;
  }

  public Path getPublicKeyPath() {
    return publicKeyPath;
  }

  public Path getPrivateKeyPath() {
    return privateKeyPath;
  }

  public InlineKeypair getInlineKeypair() {
    return inlineKeypair;
  }

  private void loadKeys() {
    this.inlineKeypair =
        new InlineKeypair(
            IOCallback.execute(() -> new String(Files.readAllBytes(this.publicKeyPath), UTF_8)),
            JaxbUtil.unmarshal(
                IOCallback.execute(() -> Files.newInputStream(privateKeyPath)),
                KeyDataConfig.class),
            keyEncryptor);
  }
}
