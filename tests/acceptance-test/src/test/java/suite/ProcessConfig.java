package suite;

import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.test.DBType;
import java.lang.annotation.*;

/**
 * Sets the runtime properties of the nodes used during the test This allows the same tests to run
 * with different node configurations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ProcessConfig {

  DBType dbType();

  CommunicationType communicationType();

  String p2pCommunicationType() default "NONE";

  SocketType socketType();

  EnclaveType enclaveType() default EnclaveType.LOCAL;

  boolean admin() default false;

  String prefix() default "";

  boolean p2pSsl() default false;

  EncryptorType encryptorType();

  ClientMode clientMode() default ClientMode.TESSERA;
}
