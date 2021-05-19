package suite;

import java.util.ArrayList;
import java.util.List;

public class NodeId {

  public static String generate(ExecutionContext executionContext) {

    List<String> tokens = new ArrayList<>();
    executionContext.getPrefix().ifPresent(v -> tokens.add(v));
    tokens.add(executionContext.getCommunicationType().name().toLowerCase());
    tokens.add(executionContext.getSocketType().name().toLowerCase());
    tokens.add(executionContext.getEncryptorType().name().toLowerCase());
    return String.join("-", tokens);
  }

  public static String generate(ExecutionContext executionContext, NodeAlias alias) {
    List<String> tokens = new ArrayList<>();

    if (executionContext.isAdmin()) {
      tokens.add("admin");
    }
    executionContext.getPrefix().ifPresent(v -> tokens.add(v));
    tokens.add(executionContext.getCommunicationType().name().toLowerCase());
    tokens.add(executionContext.getSocketType().name().toLowerCase());
    tokens.add(executionContext.getEnclaveType().name().toLowerCase());
    tokens.add(executionContext.getDbType().name().toLowerCase());
    tokens.add(executionContext.getEncryptorType().name().toLowerCase());
    tokens.add(alias.name().toLowerCase());

    return String.join("-", tokens);
  }
}
