package suite;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;

import java.lang.annotation.*;

/**
 * Sets the runtime properties of the nodes used during the test This allows the same tests to run with different node
 * configurations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ProcessConfig {

    DBType dbType();

    CommunicationType communicationType();

    SocketType socketType();

    EnclaveType enclaveType() default EnclaveType.LOCAL;

    boolean admin() default false;

    String prefix() default "";
}
