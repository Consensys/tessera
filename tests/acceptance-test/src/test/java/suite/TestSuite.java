package suite;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;
import db.DatabaseServer;
import exec.EnclaveExecManager;
import exec.ExecManager;
import exec.NodeExecManager;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class TestSuite extends Suite {
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface ProcessConfig {
        
        DBType dbType();
        
        CommunicationType communicationType();
        
        SocketType socketType();
        
        EnclaveType enclaveType() default EnclaveType.LOCAL;
        
        boolean admin() default false;
    }
    
    public TestSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
    }
    
    @Override
    public void run(RunNotifier notifier) {
        final List<ExecManager> executors = new ArrayList<>();
        try {
            ProcessConfig testConfig = Arrays.stream(getRunnerAnnotations())
                .filter(ProcessConfig.class::isInstance)
                .map(ProcessConfig.class::cast)
                .findAny()
                .orElseThrow(() -> new AssertionError("No Test config found"));
            
            ExecutionContext executionContext = ExecutionContext.Builder.create()
                .with(testConfig.communicationType())
                .with(testConfig.dbType())
                .with(testConfig.socketType())
                .with(testConfig.enclaveType())
                .withAdmin(testConfig.admin())
                .createAndSetupContext();
            
            if (executionContext.getEnclaveType() == EnclaveType.REMOTE) {
                
                executionContext.getConfigs().stream()
                    .map(EnclaveExecManager::new)
                    .forEach(exec -> {
                        exec.start();
                        executors.add(exec);
                    });
            }
            
            String nodeId = NodeId.generate(executionContext);
            DatabaseServer databaseServer = testConfig.dbType().createDatabaseServer(nodeId);
            databaseServer.start();
            
            executionContext.getConfigs().stream()
                .map(NodeExecManager::new)
                .forEach(exec -> {
                    exec.start();
                    executors.add(exec);
                });
            
            super.run(notifier);
            
            try {
                ExecutionContext.destoryContext();
            } finally {
                databaseServer.stop();
            }
        } catch (Throwable ex) {
            Description de = Description.createSuiteDescription(getTestClass().getJavaClass());
            notifier.fireTestFailure(new Failure(de, ex));
            
        } finally {
            executors.forEach(ExecManager::stop);
            
        }
        
    }
    
}
