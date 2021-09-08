package config;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.test.DBType;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.EnclaveType;
import suite.ExecutionContext;
import suite.NodeAlias;
import suite.NodeId;
import suite.SocketType;

public class ConfigGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigGenerator.class);

  public List<ConfigDescriptor> generateConfigs(ExecutionContext executionContext) {
    Objects.requireNonNull(executionContext, "Execution context is required");
    Path path = calculatePath(executionContext);
    try {
      Files.createDirectories(path);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }

    List<Config> configs = createConfigs(executionContext);

    List<Config> enclaveConfigs;
    if (executionContext.getEnclaveType() == EnclaveType.REMOTE) {
      enclaveConfigs = configs.stream().map(this::createEnclaveConfig).collect(Collectors.toList());
      configs.forEach(c -> c.setKeys(null));
    } else {
      enclaveConfigs = Collections.emptyList();
    }
    // Remove keys
    List<ConfigDescriptor> configList = new ArrayList<>();

    for (NodeAlias alias : NodeAlias.values()) {
      int i = alias.ordinal();
      final Config config = configs.get(i);

      final String filename = String.format("config%d.json", (i + 1));
      final Path ouputFile = path.resolve(filename);

      try (OutputStream out = Files.newOutputStream(ouputFile)) {
        JaxbUtil.marshalWithNoValidation(config, out);

      } catch (IOException ex) {
        throw new UncheckedIOException(ex);
      }

      final Config enclaveConfig;
      final Path enclaveOuputFile;
      if (!enclaveConfigs.isEmpty()) {
        enclaveConfig = enclaveConfigs.get(i);
        String enclaveFilename = String.format("enclave%d.json", (i + 1));
        enclaveOuputFile = path.resolve(enclaveFilename);
        try (OutputStream enclaveout = Files.newOutputStream(enclaveOuputFile)) {
          JaxbUtil.marshalWithNoValidation(enclaveConfig, enclaveout);

        } catch (IOException ex) {
          throw new UncheckedIOException(ex);
        }
      } else {
        enclaveConfig = null;
        enclaveOuputFile = null;
      }

      configList.add(
          new ConfigDescriptor(alias, ouputFile, config, enclaveConfig, enclaveOuputFile));
    }

    return configList;
  }

  Config createEnclaveConfig(Config config) {

    final Config enclaveConfig = new Config();

    ServerConfig serverConfig =
        config.getServerConfigs().stream()
            .filter(s -> s.getApp() == AppType.ENCLAVE)
            .findAny()
            .get();

    enclaveConfig.setServerConfigs(Arrays.asList(serverConfig));

    enclaveConfig.setKeys(config.getKeys());
    enclaveConfig.setAlwaysSendTo(config.getAlwaysSendTo());
    enclaveConfig.setEncryptor(config.getEncryptor());
    return enclaveConfig;
  }

  public static Path calculatePath(ExecutionContext executionContext) {

    URI baseUri = Paths.get("build/").toAbsolutePath().toUri();

    return executionContext
        .getPrefix()
        .map(v -> Paths.get(baseUri).resolve(v))
        .orElse(Paths.get(baseUri))
        .resolve(executionContext.getCommunicationType().name().toLowerCase())
        .resolve(executionContext.getSocketType().name().toLowerCase())
        .resolve(executionContext.getDbType().name().toLowerCase())
        .resolve("enclave-" + executionContext.getEnclaveType().name().toLowerCase());
  }

  private static Map<Integer, SortedMap<String, String>> keyLookup(EncryptorType encryptorType) {
    final Encryptor encryptor = transaction.utils.Utils.getEncryptor(encryptorType);
    return new HashMap<Integer, SortedMap<String, String>>() {
      {
        put(
            1,
            new TreeMap<String, String>() {
              KeyPair pair = encryptor.generateNewKeys();

              {
                put(pair.getPublicKey().encodeToBase64(), pair.getPrivateKey().encodeToBase64());
              }
            });

        put(
            2,
            new TreeMap<String, String>() {
              KeyPair pair = encryptor.generateNewKeys();

              {
                put(pair.getPublicKey().encodeToBase64(), pair.getPrivateKey().encodeToBase64());
              }
            });

        put(
            3,
            new TreeMap<String, String>() {
              KeyPair pair = encryptor.generateNewKeys();
              KeyPair pair2 = encryptor.generateNewKeys();

              {
                put(pair.getPublicKey().encodeToBase64(), pair.getPrivateKey().encodeToBase64());
                put(pair2.getPublicKey().encodeToBase64(), pair2.getPrivateKey().encodeToBase64());
              }
            });

        put(
            4,
            new TreeMap<String, String>() {
              KeyPair pair = encryptor.generateNewKeys();

              {
                put(pair.getPublicKey().encodeToBase64(), pair.getPrivateKey().encodeToBase64());
              }
            });
      }
    };
  }

  public List<Config> createConfigs(ExecutionContext executionContext) {

    final PortUtil port = new PortUtil(50520);
    final String nodeId = NodeId.generate(executionContext);
    final FeatureToggles toggles = new FeatureToggles();
    toggles.setEnableRemoteKeyValidation(true);

    final FeatureToggles enhancedPrivacyEnabledToggle = new FeatureToggles();
    enhancedPrivacyEnabledToggle.setEnableRemoteKeyValidation(true);
    enhancedPrivacyEnabledToggle.setEnablePrivacyEnhancements(true);

    final Integer partyInfoInterval = 5000;

    EncryptorType encryptorType = executionContext.getEncryptorType();
    EncryptorConfig encryptorConfig =
        new EncryptorConfig() {
          {
            setType(encryptorType);
          }
        };

    Map<Integer, SortedMap<String, String>> keyLookUp = keyLookup(encryptorType);

    // Node A,B,C have enhanced private enabled, node D does not

    // Only node A has a third party server

    Config first =
        new ConfigBuilder()
            .withNodeId(nodeId)
            .withNodeNumber(1)
            .withExecutionContext(executionContext)
            .withQt2Port(port.nextPort())
            .withP2pPort(port.nextPort())
            .withThirdPartyPort(port.nextPort())
            .withEnclavePort(port.nextPort())
            .withPartyInfoInterval(partyInfoInterval)
            .withKeys(keyLookUp.get(1))
            .withFeatureToggles(enhancedPrivacyEnabledToggle)
            .withEncryptorConfig(encryptorConfig)
            .build();

    Config second =
        new ConfigBuilder()
            .withNodeId(nodeId)
            .withNodeNumber(2)
            .withExecutionContext(executionContext)
            .withQt2Port(port.nextPort())
            .withP2pPort(port.nextPort())
            .withEnclavePort(port.nextPort())
            .withPartyInfoInterval(partyInfoInterval)
            .withKeys(keyLookUp.get(2))
            .withFeatureToggles(enhancedPrivacyEnabledToggle)
            .withEncryptorConfig(encryptorConfig)
            .build();

    Config third =
        new ConfigBuilder()
            .withNodeId(nodeId)
            .withNodeNumber(3)
            .withExecutionContext(executionContext)
            .withQt2Port(port.nextPort())
            .withP2pPort(port.nextPort())
            .withEnclavePort(port.nextPort())
            .withPartyInfoInterval(partyInfoInterval)
            .withAlwaysSendTo(keyLookUp.get(1).keySet().iterator().next())
            .withKeys(keyLookUp.get(3))
            .withFeatureToggles(enhancedPrivacyEnabledToggle)
            .withEncryptorConfig(encryptorConfig)
            .build();

    Config fourth =
        new ConfigBuilder()
            .withNodeId(nodeId)
            .withNodeNumber(4)
            .withExecutionContext(executionContext)
            .withQt2Port(port.nextPort())
            .withP2pPort(port.nextPort())
            .withEnclavePort(port.nextPort())
            .withPartyInfoInterval(partyInfoInterval)
            .withKeys(keyLookUp.get(4))
            .withFeatureToggles(toggles)
            .withEncryptorConfig(encryptorConfig)
            .build();

    first.addPeer(new Peer(second.getP2PServerConfig().getServerAddress()));
    second.addPeer(new Peer(third.getP2PServerConfig().getServerAddress()));
    third.addPeer(new Peer(fourth.getP2PServerConfig().getServerAddress()));
    fourth.addPeer(new Peer(first.getP2PServerConfig().getServerAddress()));

    List<Config> configList = List.of(first, second, third, fourth);
    if (LOGGER.isDebugEnabled()) {
      configList.stream()
          .map(JaxbUtil::marshalToString)
          .forEach(
              s -> {
                LOGGER.debug(s);
              });
    }
    return configList;
  }

  public static void main(String[] args) throws Exception {

    System.setProperty(
        "jakarta.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
    System.setProperty(
        "jakarta.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

    ExecutionContext executionContext =
        ExecutionContext.Builder.create()
            .with(CommunicationType.REST)
            .with(DBType.H2)
            .with(SocketType.UNIX)
            .with(EnclaveType.LOCAL)
            .prefix("p2p")
            .build();

    Path path = ConfigGenerator.calculatePath(executionContext);

    List<Config> configs = new ConfigGenerator().createConfigs(executionContext);

    for (int i = 1; i <= configs.size(); i++) {
      String filename = String.format("config%d.json", i);
      Path outputFile = path.resolve(filename);
      System.out.println(outputFile);
    }
  }
}
