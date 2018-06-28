
package com.github.nexus.config;

import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class LegacyPrivateKeyFileAdapter extends XmlAdapter<String, LegacyPrivateKeyFile> {

    @Override
    public LegacyPrivateKeyFile unmarshal(final String path) throws IOException {

        final Path file = Paths.get(path);
        final String json = new String(Files.readAllBytes(file));

        final JsonObject topLevelObject = Json.createReader(new StringReader(json)).readObject();
        final JsonObject data = topLevelObject.getJsonObject("data");

        if (topLevelObject.getString("type").equals("unlocked")) {

            return new LegacyPrivateKeyFile(
                data.getString("bytes"), PrivateKeyType.UNLOCKED, null, null, null, null
            );

        } else {
            final JsonObject aopts = data.getJsonObject("aopts");

            return new LegacyPrivateKeyFile(
                null,
                PrivateKeyType.LOCKED,
                new ArgonOptions(
                    aopts.getString("variant"),
                    aopts.getInt("iterations"),
                    aopts.getInt("memory"),
                    aopts.getInt("parallelism")
                ),
                data.getString("snonce"),
                data.getString("asalt"),
                data.getString("sbox")
            );

        }

    }

    @Override
    public String marshal(final LegacyPrivateKeyFile input) {
        throw new UnsupportedOperationException("Cannot marshal to file");
    }

}
