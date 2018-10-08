package com.quorum.tessera.key;

import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface KeyUtil {

    static String encodeToBase64(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getKeyBytes());
    }

    static String encodeToBase64(PrivateKey key) {
        return Base64.getEncoder().encodeToString(key.getKeyBytes());
    }

    static List<PublicKey> convert(List<String> values) {
        return Objects.requireNonNull(values, "Key values cannot be null")
                .stream()
                .map(v -> Base64.getDecoder().decode(v))
                .map(PublicKey::from)
                .collect(Collectors.toList());
    }
}
