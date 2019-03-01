package suite;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class TestSuite extends Suite {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface TestConfig {

        DBType dbType();

        CommunicationType communicationType();

        SocketType socketType();
    }

    public TestSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
    }

    
    
    @Override
    public void run(RunNotifier notifier) {

       TestConfig testConfig =Arrays.stream(getRunnerAnnotations())
                .filter(TestConfig.class::isInstance)
                .map(TestConfig.class::cast)
                .findAny().get();
        
       ExecutionContext.Builder.create()
               .with(testConfig.communicationType())
               .with(testConfig.dbType())
               .with(testConfig.socketType()).build();
       
        super.run(notifier); //To change body of generated methods, choose Tools | Templates.
        
        
        ExecutionContext.destoryContext();

    }

}
