package config;

import static java.util.stream.Collectors.toList;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.KeyDataUtil;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import suite.NodeAlias;

public class ConfigDescriptor {

  private final NodeAlias alias;
  private final Path path;
  private final Config config;
  private final Config enclaveConfig;
  private final Path enclavePath;
  private final KeyEncryptor keyEncryptor;

  public ConfigDescriptor(
      NodeAlias alias, Path path, Config config, Config enclaveConfig, Path enclavePath) {
    this.alias = alias;
    this.path = path;
    this.config = config;
    this.enclaveConfig = enclaveConfig;
    this.enclavePath = enclavePath;
    this.keyEncryptor = KeyEncryptorFactory.newFactory().create(config.getEncryptor());
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
      return enclaveConfig.getKeys().getKeyData().stream()
          .findFirst()
          .map(kd -> KeyDataUtil.unmarshal(kd, keyEncryptor))
          .get();
    } else {
      return config.getKeys().getKeyData().stream()
          .findFirst()
          .map(kd -> KeyDataUtil.unmarshal(kd, keyEncryptor))
          .get();
    }
  }

  public List<ConfigKeyPair> getAllKeys() {
    if (isEnclave()) {
      return enclaveConfig.getKeys().getKeyData().stream()
          .map(kd -> KeyDataUtil.unmarshal(kd, keyEncryptor))
          .collect(toList());
    }

    return config.getKeys().getKeyData().stream()
        .map(kd -> KeyDataUtil.unmarshal(kd, keyEncryptor))
        .collect(toList());
  }
}
