package suite;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;
import java.util.Objects;
import java.util.stream.Stream;

public class ExecutionContext {

    private final DBType dbType;

    private final CommunicationType communicationType;

    private final SocketType socketType;

    private ExecutionContext(DBType dbType, CommunicationType communicationType, SocketType socketType) {
        this.dbType = dbType;
        this.communicationType = communicationType;
        this.socketType = socketType;
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

    static class Builder {

        private DBType dbType;

        private CommunicationType communicationType;

        private SocketType socketType;

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

        protected void createAndSetupContext() {

            Stream.of(dbType, communicationType, socketType)
                    .forEach(Objects::requireNonNull);

            ExecutionContext executionContext = new ExecutionContext(dbType, communicationType, socketType);

            if (THREAD_SCOPE.get() != null) {
                throw new IllegalStateException("Context has already been created");
            }

            THREAD_SCOPE.set(executionContext);
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
