package com.github.nexus.config.adapters;

import com.github.nexus.config.ArgonOptions;
import com.github.nexus.config.PrivateKey;

import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;

import static com.github.nexus.config.PrivateKeyType.LOCKED;
import static com.github.nexus.config.PrivateKeyType.UNLOCKED;

/**
 * Converts between a {@link PrivateKeyMutable} and {@link PrivateKey}
 *
 * After unmarshalling, the private key will have all the data it needs to be used
 * (either directly or after decryption)
 *
 * The {@link PrivateKeyMutable} is the type unmarshalled, but will have various properties
 * set or not set based on what the user has provided, and needs further configuration handling
 * (which the adapter provides)
 *
 * The adapter first looks for a legacyPath, then for a raw path, then for all other options
 */
public class PrivateKeyAdapter extends XmlAdapter<PrivateKeyMutable, PrivateKey> {

    @Override
    public PrivateKey unmarshal(final PrivateKeyMutable input) throws IOException {

        if (input.getPath() != null) {

            return this.handleJsonFile(input);

        } else if (input.getRawPath() != null) {

            final byte[] keyBytes = Files.readAllBytes(input.getRawPath());
            final String key = new String(keyBytes);

            return new PrivateKey(key, null, UNLOCKED, null, null, null, null, input.getPath());

        } else if (input.getValue() != null) {

            return new PrivateKey(input.getValue(), null, UNLOCKED, null, null, null, null, null);

        } else {

            return new PrivateKey(
                input.getContents().getValue(),
                input.getPassword(),
                input.getContents().getType(),
                input.getContents().getSnonce(),
                input.getContents().getAsalt(),
                input.getContents().getSbox(),
                input.getContents().getArgonOptions(),
                input.getContents().getPath()
            );

        }

    }

    private PrivateKey handleJsonFile(final PrivateKeyMutable input) throws IOException {

        if (!Files.exists(input.getPath())) {
            return new PrivateKey(null, null, null, null, null, null, null, input.getPath());
        }

        final String json = new String(Files.readAllBytes(input.getPath()));

        final JsonObject topLevelObject = Json.createReader(new StringReader(json)).readObject();
        final JsonObject data = topLevelObject.getJsonObject("data");

        if (topLevelObject.getString("type").equals(UNLOCKED.name().toLowerCase())) {

            return new PrivateKey(data.getString("bytes"), null, UNLOCKED, null, null, null, null, input.getPath());

        } else {
            final JsonObject aopts = data.getJsonObject("aopts");

            return new PrivateKey(
                null,
                input.getPassword(),
                LOCKED,
                data.getString("snonce"),
                data.getString("asalt"),
                data.getString("sbox"),
                new ArgonOptions(
                    aopts.getString("variant"),
                    aopts.getInt("iterations"),
                    aopts.getInt("memory"),
                    aopts.getInt("parallelism")
                ), input.getPath()
            );

        }
    }

    @Override
    public PrivateKeyMutable marshal(final PrivateKey input) {
        return new PrivateKeyMutable(input.getPath(), null, input.getValue(), input.getPassword(), input);
    }
}
