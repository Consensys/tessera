package suite;

import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.DBType;
import config.ConfigDescriptor;
import config.ConfigGenerator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionContext.class);

  private final DBType dbType;

  private final CommunicationType communicationType;

  private final CommunicationType p2pCommunicationType;

  private final boolean p2pSsl;

  private final SocketType socketType;

  private final EnclaveType enclaveType;

  private List<ConfigDescriptor> configs;

  private boolean admin;

  private String prefix;

  private EncryptorType encryptorType;

  private ClientMode clientMode;

  private boolean autoCreateTables;

  private ExecutionContext(
      DBType dbType,
      CommunicationType communicationType,
      SocketType socketType,
      EnclaveType enclaveType,
      boolean admin,
      String prefix,
      CommunicationType p2pCommunicationType,
      boolean p2pSsl,
      EncryptorType encryptorType,
      ClientMode clientMode,
      boolean autoCreateTables) {
    this.dbType = dbType;
    this.communicationType = communicationType;
    this.socketType = socketType;
    this.enclaveType = enclaveType;
    this.admin = admin;
    this.prefix = prefix;
    this.p2pCommunicationType = p2pCommunicationType;
    this.p2pSsl = p2pSsl;
    this.encryptorType = encryptorType;
    this.clientMode = clientMode;
    this.autoCreateTables = autoCreateTables;
  }

  public DBType getDbType() {
    return dbType;
  }

  public CommunicationType getCommunicationType() {
    return communicationType;
  }

  public SocketType getSocketType() {
    return socketType;
  }

  public EnclaveType getEnclaveType() {
    return enclaveType;
  }

  public List<ConfigDescriptor> getConfigs() {
    return configs;
  }

  public boolean isAdmin() {
    return admin;
  }

  public Optional<String> getPrefix() {
    return Optional.ofNullable(prefix);
  }

  public CommunicationType getP2pCommunicationType() {
    return p2pCommunicationType;
  }

  public boolean isP2pSsl() {
    return p2pSsl;
  }

  public EncryptorType getEncryptorType() {
    return encryptorType;
  }

  public ClientMode getClientMode() {
    return clientMode;
  }

  public boolean isAutoCreateTables() {
    return autoCreateTables;
  }

  public static class Builder {

    private DBType dbType;

    private CommunicationType communicationType;

    private SocketType socketType;

    private EnclaveType enclaveType;

    private String prefix;

    private CommunicationType p2pCommunicationType;

    private boolean p2pSsl = false;

    private EncryptorType encryptorType;

    private ClientMode clientMode;

    private boolean autoCreateTables = true;

    private Builder() {}

    public static Builder create() {
      return new Builder();
    }

    public Builder with(EncryptorType encryptorType) {
      this.encryptorType = encryptorType;
      return this;
    }

    public Builder with(DBType dbType) {
      this.dbType = dbType;
      return this;
    }

    public Builder with(SocketType socketType) {
      this.socketType = socketType;
      return this;
    }

    public Builder with(CommunicationType communicationType) {
      this.communicationType = communicationType;
      return this;
    }

    public Builder withP2pCommunicationType(CommunicationType p2pCommunicationType) {
      this.p2pCommunicationType = p2pCommunicationType;
      return this;
    }

    public Builder with(EnclaveType enclaveType) {
      this.enclaveType = enclaveType;
      return this;
    }

    public Builder prefix(String prefix) {
      this.prefix = Objects.equals("", prefix) ? null : prefix;
      return this;
    }

    private boolean admin;

    public Builder withAdmin(boolean admin) {
      this.admin = admin;
      return this;
    }

    public Builder withP2pSsl(boolean p2pSsl) {
      this.p2pSsl = p2pSsl;
      return this;
    }

    public Builder with(ClientMode clientMode) {
      this.clientMode = clientMode;
      return this;
    }

    public Builder withAutoCreateTables(boolean autoCreateTables) {
      this.autoCreateTables = autoCreateTables;
      return this;
    }

    public ExecutionContext build() {
      Stream.of(dbType, communicationType, socketType, enclaveType, encryptorType)
          .forEach(Objects::requireNonNull);

      this.p2pCommunicationType =
          Optional.ofNullable(p2pCommunicationType).orElse(communicationType);

      ExecutionContext executionContext =
          new ExecutionContext(
              dbType,
              communicationType,
              socketType,
              enclaveType,
              admin,
              prefix,
              p2pCommunicationType,
              p2pSsl,
              encryptorType,
              clientMode,
              autoCreateTables);

      return executionContext;
    }

    public ExecutionContext buildAndStoreContext() {

      ExecutionContext executionContext = build();

      if (THREAD_SCOPE.get() != null) {
        throw new IllegalStateException("Context has already been created");
      }

      THREAD_SCOPE.set(executionContext);

      return THREAD_SCOPE.get();
    }

    public ExecutionContext createAndSetupContext() {

      Stream.of(dbType, communicationType, socketType, enclaveType, encryptorType, clientMode)
          .forEach(Objects::requireNonNull);

      ExecutionContext executionContext = build();

      List<ConfigDescriptor> configs = new ConfigGenerator().generateConfigs(executionContext);
      configs.stream()
          .map(ConfigDescriptor::getConfig)
          .forEach(
              c -> {
                LOGGER.debug("Generated config {}", JaxbUtil.marshalToStringNoValidation(c));
              });
      // FIXME: YUk
      executionContext.configs = configs;

      if (THREAD_SCOPE.get() != null) {
        throw new IllegalStateException("Context has already been created");
      }

      THREAD_SCOPE.set(executionContext);

      return THREAD_SCOPE.get();
    }
  }

  private static final ThreadLocal<ExecutionContext> THREAD_SCOPE =
      new ThreadLocal<ExecutionContext>();

  public static ExecutionContext currentContext() {
    if (Objects.isNull(THREAD_SCOPE.get())) {
      throw new IllegalStateException("Execution context has not been initialised");
    }
    return THREAD_SCOPE.get();
  }

  public static void destroyContext() {
    THREAD_SCOPE.remove();
  }
}
