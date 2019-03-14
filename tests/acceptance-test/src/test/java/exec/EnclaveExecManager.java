package exec;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.ProcessManager;
import config.ConfigGenerator;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import suite.ServerStatusCheck;
import suite.ServerStatusCheckExecutor;

public class EnclaveExecManager {

    private ConfigGenerator.ConfigDescriptor configDescriptor;

    public EnclaveExecManager(ConfigGenerator.ConfigDescriptor configDescriptor) {
        this.configDescriptor = configDescriptor;
    }

    private final URL logbackConfigFile = ProcessManager.class.getResource("/logback-enclave.xml");
    
    public Process start() {

        Path enclaveServerJar = Paths.get(System.getProperty("enclave.jaxrs.server.jar", "../../enclave/enclave-jaxrs/target/enclave-jaxrs-0.9-SNAPSHOT-server.jar"));

        ServerConfig serverConfig = configDescriptor.getConfig().getServerConfigs().get(0);
        
        List<String> cmd = new ExecArgsBuilder()
                .withJvmArg("-Dnode.number=enclave")
                .withJvmArg("-Dlogback.configurationFile="+ logbackConfigFile)
                .withExecutableJarFile(enclaveServerJar)
                .withConfigFile(configDescriptor.getPath())
                .build();

        Process process = ExecUtils.start(cmd);

        ServerStatusCheckExecutor serverStatusCheckExecutor = new ServerStatusCheckExecutor(ServerStatusCheck.create(serverConfig));

        ExecutorService executorService = Executors.newCachedThreadPool();

        Future<Boolean> future = ExecCallback.doExecute(() -> executorService.submit(serverStatusCheckExecutor));

        Boolean result = ExecCallback.doExecute(() -> future.get(30, TimeUnit.SECONDS));

        if (!result) {
            throw new IllegalStateException("Enclave server not started");
        }

        return process;

    }

    public static void main(String[] args) throws Exception {

        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        final Config enclaveConfig = new Config();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.ENCLAVE);
        serverConfig.setBindingAddress("http://0.0.0.0:8989");
        serverConfig.setServerAddress("http://localhost:8989");
        serverConfig.setCommunicationType(CommunicationType.REST);

        enclaveConfig.setServerConfigs(Arrays.asList(serverConfig));

        enclaveConfig.setKeys(new KeyConfiguration());
        enclaveConfig.getKeys().setKeyData(Arrays.asList(new DirectKeyPair("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=")));

        Path configFile = Paths.get("target", UUID.randomUUID().toString() + ".json");

        try (OutputStream out = Files.newOutputStream(configFile)){
            JaxbUtil.marshalWithNoValidation(enclaveConfig, out);
        }

        //EnclaveExecManager enclaveExecManager = new EnclaveExecManager(configFile);
        // enclaveExecManager.start(serverConfig);
    }

}
