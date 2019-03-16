package suite;

import java.util.stream.Collectors;
import java.util.stream.Stream;


public class NodeId {
 
    public static String generate(ExecutionContext executionContext) {
        return executionContext.getCommunicationType().name().toLowerCase()
                + "-" + executionContext.getSocketType().name().toLowerCase();
    }
    
    
    public static String generate(ExecutionContext executionContext,NodeAlias alias) {
        return Stream.of(
                executionContext.getCommunicationType(),
                executionContext.getSocketType(),
                executionContext.getEnclaveType(),
                alias)
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining("-"));
    }
    
}
