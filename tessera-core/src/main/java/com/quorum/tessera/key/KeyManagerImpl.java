package com.quorum.tessera.key;

import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.key.exception.KeyNotFoundException;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.NaclKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class KeyManagerImpl implements KeyManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyManagerImpl.class);

    /**
     * A list of all pub/priv keys that are attached to this node
     */
    private final Set<NaclKeyPair> localKeys;
    
    private final NaclKeyPair defaultKeys;
    
    private final Set<Key> forwardingPublicKeys;
    
    public KeyManagerImpl(final Collection<ConfigKeyPair> keys, Collection<Key> forwardKeys) {
        this.localKeys = keys
                .stream()
                .map(kd
                        -> new NaclKeyPair(
                        new Key(Base64.getDecoder().decode(kd.getPublicKey())),
                        new Key(Base64.getDecoder().decode(kd.getPrivateKey()))
                )
                ).collect(Collectors.toSet());
        
        this.defaultKeys = localKeys.iterator().next();
        
        this.forwardingPublicKeys = Collections.unmodifiableSet(new HashSet<>(forwardKeys));
    }
    
    @Override
    public PublicKey getPublicKeyForPrivateKey(final PrivateKey privateKey) {
        LOGGER.debug("Attempting to find public key for the private key {}", privateKey);
        //TODO: 
        Key naclPRivateKey = new Key(privateKey.getKeyBytes());
        final PublicKey publicKey = localKeys
                .stream()
                .filter(keypair -> Objects.equals(keypair.getPrivateKey(), naclPRivateKey))
                .findFirst()
                .map(NaclKeyPair::getPublicKey)
                .map(key -> PublicKey.from(key.getKeyBytes()))
                .orElseThrow(
                        () -> new KeyNotFoundException("Private key " + Base64.getEncoder().encodeToString(privateKey.getKeyBytes()) 
                                + " not found when searching for public key")
                );
        
        LOGGER.debug("Found public key {} for private key {}", publicKey, privateKey);
        
        return publicKey;
    }
    
    
    @Override
    public PrivateKey getPrivateKeyForPublicKey(final PublicKey publicKey) {
        LOGGER.debug("Attempting to find private key for the public key {}", publicKey);
        //TODO: 
        Key naclPublicKey = new Key(publicKey.getKeyBytes());
        final PrivateKey privateKey = localKeys
                .stream()
                .filter(keypair -> Objects.equals(keypair.getPublicKey(), naclPublicKey))
                .findFirst()
                .map(NaclKeyPair::getPrivateKey)
                .map(key -> PrivateKey.from(key.getKeyBytes()))
                .orElseThrow(
                        () -> new KeyNotFoundException("Public key " + Base64.getEncoder().encodeToString(publicKey.getKeyBytes()) 
                                + " not found when searching for private key")
                );
        
        LOGGER.debug("Found private key {} for public key {}", privateKey, publicKey);
        
        return privateKey;
    }
    
    @Override
    public Set<Key> getPublicKeys() {
        return localKeys
                .stream()
                .map(NaclKeyPair::getPublicKey)
                .collect(Collectors.toSet());
    }
    
    @Override
    public Key defaultPublicKey() {
        return defaultKeys.getPublicKey();
    }
    
    @Override
    public Set<Key> getForwardingKeys() {
        return this.forwardingPublicKeys;
    }
    
}
