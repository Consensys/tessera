package config;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import java.nio.file.Path;
import java.util.Optional;
import suite.NodeAlias;

public class ConfigDescriptor {

    private final NodeAlias alias;
    private final Path path;
    private final Config config;
    private final Config enclaveConfig;
    private final Path enclavePath;

    public ConfigDescriptor(NodeAlias alias, Path path, Config config, Config enclaveConfig, Path enclavePath) {
        this.alias = alias;
        this.path = path;
        this.config = config;
        this.enclaveConfig = enclaveConfig;
        this.enclavePath = enclavePath;
    }

    public NodeAlias getAlias() {
        return alias;
    }

    public Path getPath() {
        return path;
    }

    public Config getConfig() {
        return config;
    }

    public Optional<Config> getEnclaveConfig() {
        return Optional.ofNullable(enclaveConfig);
    }

    public Path getEnclavePath() {
        return enclavePath;
    }

    public boolean isEnclave() {
        return getEnclaveConfig().isPresent();
    }

    public ConfigKeyPair getKey() {
        if (isEnclave()) {
            return enclaveConfig.getKeys().getKeyData().get(0);
        } else {
            return config.getKeys().getKeyData().get(0);
        }
    }
}
