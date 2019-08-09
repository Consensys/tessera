package suite;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NodeId {

    public static String generate(ExecutionContext executionContext) {
        return generate(executionContext, null);
    }

    public static String generate(ExecutionContext executionContext, NodeAlias alias) {
        if (executionContext.isAdmin()) {
            return "admin";
        }
        List<String> tokens = new ArrayList<>();
        executionContext.getPrefix().ifPresent(v -> tokens.add(v));
        tokens.add("q2t");
        tokens.add(executionContext.getCommunicationType().name().toLowerCase());
        tokens.add(executionContext.getSocketType().name().toLowerCase());
        tokens.add("p2p");
        tokens.add(executionContext.getP2pCommunicationType().name().toLowerCase());
        tokens.add("db");
        tokens.add(executionContext.getDbType().name().toLowerCase());
        tokens.add("enclave");
        tokens.add(executionContext.getEnclaveType().name().toLowerCase());
        if (Objects.nonNull(alias)) {
            tokens.add(alias.name().toLowerCase());
        }
        return String.join("-", tokens);
    }
}
