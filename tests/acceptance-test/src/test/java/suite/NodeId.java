package suite;


public class NodeId {
 
    public static String generate(ExecutionContext executionContext) {
        return executionContext.getCommunicationType().name().toLowerCase()
                + "-" + executionContext.getSocketType().name().toLowerCase();
    }
    
}
