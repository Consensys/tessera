package com.quorum.tessera.encryption;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface KeyFactory {

    static List<PublicKey> convert(List<String> values) {
        return Objects.requireNonNull(values, "Key values cannot be null")
                .stream()
                .map(v -> Base64.getDecoder().decode(v))
                .map(PublicKey::from)
                .collect(Collectors.toList());
    }
}
