package suite;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;
import config.ConfigDescriptor;
import config.ConfigGenerator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ExecutionContext {

    private final DBType dbType;

    private final CommunicationType communicationType;

    private final SocketType socketType;

    private final EnclaveType enclaveType;

    private List<ConfigDescriptor> configs;

    private boolean admin;

    private String prefix;

    private ExecutionContext(DBType dbType,
        CommunicationType communicationType,
        SocketType socketType,
        EnclaveType enclaveType, boolean admin,String prefix) {
        this.dbType = dbType;
        this.communicationType = communicationType;
        this.socketType = socketType;
        this.enclaveType = enclaveType;
        this.admin = admin;
        this.prefix = prefix;
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

    public static class Builder {

        private DBType dbType;

        private CommunicationType communicationType;

        private SocketType socketType;

        private EnclaveType enclaveType;

        private String prefix;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
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

        public Builder with(EnclaveType enclaveType) {
            this.enclaveType = enclaveType;
            return this;
        }

        public Builder prefix(String prefix) {
            this.prefix = Objects.equals("",prefix) ? null : prefix;
            return this;
        }

        private boolean admin;

        public Builder withAdmin(boolean admin) {
            this.admin = admin;
            return this;
        }

        public ExecutionContext build() {
            Stream.of(dbType, communicationType, socketType, enclaveType)
                .forEach(Objects::requireNonNull);

            ExecutionContext executionContext = new ExecutionContext(dbType, communicationType, socketType, enclaveType, admin,prefix);

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

            Stream.of(dbType, communicationType, socketType, enclaveType)
                .forEach(Objects::requireNonNull);

            ExecutionContext executionContext = build();

            List<ConfigDescriptor> configs = new ConfigGenerator().generateConfigs(executionContext);

            //FIXME: YUk
            executionContext.configs = configs;

            if (THREAD_SCOPE.get() != null) {
                throw new IllegalStateException("Context has already been created");
            }

            THREAD_SCOPE.set(executionContext);

            return THREAD_SCOPE.get();
        }

    }

    private static final ThreadLocal<ExecutionContext> THREAD_SCOPE = new ThreadLocal<ExecutionContext>();

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
