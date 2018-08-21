package com.quorum.tessera.config.keys;

import com.quorum.tessera.nacl.Key;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface KeysConverter {


    static List<Key> convert(List<String> values) {
        return Objects.requireNonNull(values, "Key values cannot be null")
                .stream()
                .map(v -> Base64.getDecoder().decode(v))
                .map(Key::new)
                .collect(Collectors.toList());
    }

}
