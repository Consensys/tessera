package suite;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;
import config.ConfigGenerator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExecutionContext {

    private final DBType dbType;

    private final CommunicationType communicationType;

    private final SocketType socketType;

    private final EnclaveType enclaveType;

    private List<ConfigGenerator.ConfigDescriptor> configs;

    private ExecutionContext(DBType dbType,
            CommunicationType communicationType,
            SocketType socketType,
            EnclaveType enclaveType) {
        this.dbType = dbType;
        this.communicationType = communicationType;
        this.socketType = socketType;
        this.enclaveType = enclaveType;
    }

    public void setConfigs(List<ConfigGenerator.ConfigDescriptor> configs) {
        this.configs = configs;
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

    public List<ConfigGenerator.ConfigDescriptor> getConfigs() {
        return configs.stream().filter(c -> !c.isEnclave()).collect(Collectors.toList());
    }

    public List<ConfigGenerator.ConfigDescriptor> getEnclaveConfigs() {
        return configs.stream().filter(c -> c.isEnclave()).collect(Collectors.toList());
    }


    public static class Builder {

        private DBType dbType;

        private CommunicationType communicationType;

        private SocketType socketType;

        private EnclaveType enclaveType;

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

        public ExecutionContext build() {
            Stream.of(dbType, communicationType, socketType, enclaveType)
                    .forEach(Objects::requireNonNull);

            ExecutionContext executionContext = new ExecutionContext(dbType, communicationType, socketType, enclaveType);

            return executionContext;
        }

        protected ExecutionContext createAndSetupContext() {

            Stream.of(dbType, communicationType, socketType, enclaveType)
                    .forEach(Objects::requireNonNull);

            ExecutionContext executionContext = build();

            List<ConfigGenerator.ConfigDescriptor> configs = new ConfigGenerator().generateConfigs(executionContext);
            if(configs.isEmpty()) throw new IllegalStateException("Empty configs");
            executionContext.setConfigs(configs);

            if (THREAD_SCOPE.get() != null) {
                throw new IllegalStateException("Context has already been created");
            }

            THREAD_SCOPE.set(executionContext);

            return executionContext;
        }

    }

    private static final ThreadLocal<ExecutionContext> THREAD_SCOPE = new ThreadLocal<ExecutionContext>();

    public static ExecutionContext currentContext() {
        if (Objects.isNull(THREAD_SCOPE.get())) {
            throw new IllegalStateException("Execution context has not been initialised");
        }
        return THREAD_SCOPE.get();
    }

    protected static void destoryContext() {
        THREAD_SCOPE.remove();
    }

}
