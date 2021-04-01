package net.consensys.tessera.migration;

import com.quorum.tessera.argon2.Argon2Impl;
import com.quorum.tessera.argon2.ArgonOptions;
import com.quorum.tessera.argon2.ArgonResult;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.SharedKey;
import com.quorum.tessera.io.IOCallback;
import net.consensys.orion.config.Config;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import org.apache.tuweni.crypto.sodium.Box;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OrionKeyHelper {

    private final Map<Box.PublicKey, String> passwordLookup = new HashMap<>();

    private final Map<Path, Box.KeyPair> keyPairLookup = new HashMap<>();

    private List<String> passwords;

    private final Config orionConfig;

    private final Path filePath;

    private OrionKeyHelper(Config orionConfig, Path filePath) {
        this.orionConfig = Objects.requireNonNull(orionConfig, "Config is required");
        this.filePath = Objects.requireNonNull(filePath);

        this.unlockedPrivateKeys();
    }

    public static OrionKeyHelper from(Path filePath) {
        try {
            Config config = Config.load(filePath);
            return new OrionKeyHelper(config, filePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<Box.KeyPair> getKeyPairs() {
        return keyPairLookup.values().stream()
                .sorted(Comparator.comparing(k -> k.publicKey().hashCode()))
                .collect(Collectors.toList());
    }

    public List<String> getPasswords() {
        return passwords;
    }

    public void unlockedPrivateKeys() {
        orionConfig.passwords().filter(Files::exists).ifPresentOrElse(
            p -> this.passwords = IOCallback.execute(() -> Files.readAllLines(p)),
            () -> this.passwords = List.of()
        );

        Path baseDir = Paths.get("").toAbsolutePath();

        List<Path> privateKeyPaths = orionConfig.privateKeys().stream()
            .map(p -> Paths.get(baseDir.toString(),p.toString()))
            .collect(Collectors.toList());

        List<JsonObject> privateKeyJsonConfig =
            privateKeyPaths.stream()
                .flatMap(p -> IOCallback.execute(() -> Files.lines(p)))
                .map(StringReader::new)
                .map(Json::createReader)
                .map(JsonReader::readObject)
                .collect(Collectors.toList());

        IntStream.range(0, orionConfig.privateKeys().size())
            .forEach(
                i -> {
                    JsonObject privateKey = privateKeyJsonConfig.get(i);
                    JsonObject privateKeyData = privateKey.getJsonObject("data");
                    byte[] unlocked;
                    final String password;
                    if (privateKey.getString("type").equals("unlocked")) {
                        unlocked = Base64.getDecoder().decode(privateKeyData.getString("bytes"));
                        password = null;
                    } else {
                        byte[] data = Base64.getDecoder().decode(privateKeyData.getString("bytes"));
                        password = passwords.get(i);
                        unlocked = unlock(data, password);

                        migrateKeyToTesseraFormat(privateKeyPaths.get(i), data);
                    }

                    Path publicKeyFile = Paths.get(baseDir.toString(), orionConfig.publicKeys().get(i).toString());
                    String publicKeyData = IOCallback.execute(() -> Files.readString(publicKeyFile));
                    Box.PublicKey publicKey =
                        Box.PublicKey.fromBytes(Base64.getDecoder().decode(publicKeyData));
                    Box.SecretKey secretKey = Box.SecretKey.fromBytes(unlocked);
                    Box.KeyPair keyPair = new Box.KeyPair(publicKey, secretKey);
                    passwordLookup.put(publicKey, Optional.ofNullable(password).orElse(""));
                    keyPairLookup.put(publicKeyFile, keyPair);
                });
    }

    private void migrateKeyToTesseraFormat(Path privateKeyPath, byte[] orionPrivateKeyData) {
        final Path tesseraKeyOutputFile = privateKeyPath.resolveSibling(privateKeyPath.getFileName().toString() + ".tessera");

        PrivateKeyData privKeyComponents = new PrivateKeyData();
        Base64.Encoder encoder = Base64.getEncoder();

        privKeyComponents.setArgonOptions(new com.quorum.tessera.config.ArgonOptions("i", 3, 268435456 / 1024, 1));
        privKeyComponents.setAsalt(encoder.encodeToString(Arrays.copyOf(orionPrivateKeyData, 16)));
        privKeyComponents.setSnonce(encoder.encodeToString(Arrays.copyOf(orionPrivateKeyData, 24)));
        privKeyComponents.setSbox(encoder.encodeToString(Arrays.copyOfRange(orionPrivateKeyData, 24, orionPrivateKeyData.length)));
        KeyDataConfig tesseraKeyConfig = new KeyDataConfig(privKeyComponents, PrivateKeyType.LOCKED);

        String marshalled = JaxbUtil.marshalToStringNoValidation(tesseraKeyConfig);
        IOCallback.execute(() -> Files.writeString(tesseraKeyOutputFile, marshalled, StandardOpenOption.CREATE_NEW, StandardOpenOption.DSYNC));
    }

    static byte[] unlock(byte[] keyAsBytes, String password) {
        final byte[] extractedNonce = Arrays.copyOf(keyAsBytes, 24);

        final byte[] salt = Arrays.copyOfRange(extractedNonce, 0, 16);
        final ArgonOptions options = new ArgonOptions("i", 3, 268435456 / 1024, 1);
        final ArgonResult hash = new Argon2Impl().hash(options, password.toCharArray(), salt);

        EncryptorConfig encryptorConfig = new EncryptorConfig() {
            {
                setType(EncryptorType.NACL);
            }
        };
        Encryptor encryptor =
            EncryptorFactory.newFactory(encryptorConfig.getType().name()).create(encryptorConfig.getProperties());

        return encryptor.openAfterPrecomputation(
            Arrays.copyOfRange(keyAsBytes, 24, keyAsBytes.length), new Nonce(extractedNonce), SharedKey.from(hash.getHash()));
    }

    public Box.KeyPair findKeyPairByPublicKeyPath(Path p) {
        return keyPairLookup.get(p);
    }

    public String findOriginalKeyPasswordByPublicKeyPath(Path p) {
        Box.PublicKey publicKey = keyPairLookup.get(p).publicKey();
        return passwordLookup.get(publicKey);
    }

    Map<EncryptedKey, Box.KeyPair> findRecipientKeyPairs(EncryptedPayload encryptedPayload) {
        return Arrays.stream(encryptedPayload.encryptedKeys())
                .filter(k -> findRecipientKeyPairs(k, encryptedPayload).isPresent())
                .collect(Collectors.toMap(k -> k, k -> findRecipientKeyPairs(k, encryptedPayload).get()));
    }

    Optional<Box.KeyPair> findRecipientKeyPairs(EncryptedKey key, EncryptedPayload encryptedPayload) {
        final Box.PublicKey senderPublicKey = encryptedPayload.sender();
        final Box.Nonce nonce = Box.Nonce.fromBytes(encryptedPayload.nonce());

        return keyPairLookup.values().stream()
                // .filter(keyPair -> !Arrays.equals(keyPair.publicKey().bytesArray(),senderPublicKey.bytesArray()))
                .filter(
                        keyPair -> {
                            byte[] o = Box.decrypt(key.getEncoded(), senderPublicKey, keyPair.secretKey(), nonce);
                            return Objects.nonNull(o);
                        })
                .findFirst();
    }

    public Optional<Box.SecretKey> findPrivateKey(Box.PublicKey publicKey) {
        return getKeyPairs().stream()
                .filter(p -> p.publicKey().equals(publicKey))
                .findFirst()
                .map(Box.KeyPair::secretKey);
    }

    public Config getOrionConfig() {
        return orionConfig;
    }

    public Path getFilePath() {
        return filePath;
    }
}
