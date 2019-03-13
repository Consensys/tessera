package exec;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.enclave.rest.Main;
import com.quorum.tessera.test.DBType;
import java.nio.file.Paths;
import java.util.List;
import suite.EnclaveType;
import suite.ExecutionContext;
import suite.SocketType;

public class EnclaveExecManager {

    private ExecutionContext executionContext;

    public EnclaveExecManager(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }


    
    

    public static void main(String[] args) {
        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        ExecutionContext executionContext = ExecutionContext.Builder.create()
                .with(CommunicationType.REST)
                .with(DBType.H2)
                .with(EnclaveType.REMOTE)
                .with(SocketType.HTTP).build();

        List<String> cmd = new ExecArgsBuilder()
                .withMainClass(Main.class)
                .withExecutableJarFile(Paths.get("somejar.jar"))
                .withConfigFile(Paths.get("myfile.json"))
                .build();

        System.out.println(String.join(" ", cmd));

    }

}
