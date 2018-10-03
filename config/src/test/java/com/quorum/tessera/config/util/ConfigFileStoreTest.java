package com.quorum.tessera.config.util;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.io.FilesDelegate;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class ConfigFileStoreTest {

    private ConfigFileStore configFileStore;

    private Path path;

    @Before
    public void onSetUp() throws Exception {
        path = Files.createTempFile(UUID.randomUUID().toString(), ".junit");
        path.toFile().deleteOnExit();

        final URL sampleConfig = getClass().getResource("/sample.json");
        try (InputStream in = sampleConfig.openStream()) {
            Config initialConfig = JaxbUtil.unmarshal(in, Config.class);
            JaxbUtil.marshal(initialConfig, Files.newOutputStream(path));
        }

        configFileStore = ConfigFileStore.create(path);
    }

    @Test
    public void getReturnsSameInstance() {
        assertThat(ConfigFileStore.get()).isSameAs(configFileStore);

    }

    @Test
    public void save() throws Exception {

        final URL updatedConfig = getClass().getResource("/sample_full.json");
        try (InputStream in = updatedConfig.openStream()) {
            Config config = JaxbUtil.unmarshal(in, Config.class);
            configFileStore.save(config);
        }

        final JsonObject result = Optional.of(path)
                .map(FilesDelegate.create()::newInputStream)
                .map(Json::createReader)
                .map(JsonReader::readObject)
                .get();

        assertThat(result.getJsonObject("server").getString("hostName"))
                .isEqualTo("http://localhost");

    }

}
