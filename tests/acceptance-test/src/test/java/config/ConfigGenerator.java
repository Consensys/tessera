package config;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.DBType;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import suite.ExecutionContext;
import suite.NodeAlias;
import suite.NodeId;
import suite.SocketType;

public class ConfigGenerator {
    
    public static class ConfigDescriptor {
        
        private NodeAlias alias;
        
        private Path path;
        
        private Config config;

        public ConfigDescriptor(NodeAlias alias, Path path, Config config) {
            this.alias = alias;
            this.path = path;
            this.config = config;
        }

        public NodeAlias getAlias() {
            return alias;
        }

        public Path getPath() {
            return path;
        }

        public Config getConfig() {
            return config;
        }

    }
    

    public List<ConfigDescriptor> generateConfigs(ExecutionContext executionContext) {

        Path path = calculatePath(executionContext);
        try{
            Files.createDirectories(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        List<Config> configs = createConfigs(executionContext);
        
        List<ConfigDescriptor> configList = new ArrayList<>();
        
        for (int i = 0; i < configs.size(); i++) {
            Config config = configs.get(i);
            
            String filename = String.format("config%d.json", (i + 1));
            Path ouputFile = path.resolve(filename);

            try (OutputStream out = Files.newOutputStream(ouputFile)){
                JaxbUtil.marshalWithNoValidation(config, out);
                configList.add(new ConfigDescriptor(NodeAlias.values()[i], ouputFile, config));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
  
        return configList;

    }

    public static Path calculatePath(ExecutionContext executionContext) {
        try{
            URI baseUri = ConfigGenerator.class.getResource("/").toURI();
            return Paths.get(baseUri)
                    .resolve(executionContext.getCommunicationType().name().toLowerCase())
                    .resolve(executionContext.getSocketType().name().toLowerCase())
                    .resolve(executionContext.getDbType().name().toLowerCase())
                    .resolve("enclave-"+ executionContext.getEnclaveType().name().toLowerCase());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Map<Integer, Map<String, String>> keyLookUp = new HashMap<Integer, Map<String, String>>() {
        {
            put(1, new TreeMap<String, String>() {
                {
                    put("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
                }
            });

            put(2, new TreeMap<String, String>() {
                {
                    put("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=", "fF5UOlKKIwuaNrZ8+KU4WO+pxOYu8tNMQncyxbsSC6U=");
                }
            });

            put(3, new TreeMap<String, String>() {
                {
                    put("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=", "ygQVE998+w/C+rU/4CVgyhSAJf63YLKufbkqihcpjVI=");
                    put("jP4f+k/IbJvGyh0LklWoea2jQfmLwV53m9XoHVS4NSU=", "rVtozM4nTmiwGAtOfYBNWO+CZgubzhIdPwGLZn3HrMU=");

                }
            });

            put(4, new TreeMap<String, String>() {
                {
                    put("Tj8xg/HpsYmh7Te3UerzlLx1HgpWVOGq25ZgbwaPNVM=", "q2UeGA4o9g4rpn4+VdCELQVsbqTTBS0HCpcL/dgal24=");
                }
            });

        }
    };

    public List<Config> createConfigs(ExecutionContext executionContext) {

        AtomicInteger port = new AtomicInteger(50520);

        String nodeId = NodeId.generate(executionContext);

        Config first = new ConfigBuilder()
                .withNodeId(nodeId)
                .withNodeNumbber(1)
                .withExecutionContext(executionContext)
                .withQt2Port(port.intValue())
                .withP2pPort(port.incrementAndGet())
                .withAdminPort(port.incrementAndGet())
                .withKeys(keyLookUp.get(1))
                .build();

        Config second = new ConfigBuilder()
                .withNodeId(nodeId)
                .withNodeNumbber(2)
                .withExecutionContext(executionContext)
                .withQt2Port(port.incrementAndGet())
                .withP2pPort(port.incrementAndGet())
                .withAdminPort(port.incrementAndGet())
                .withKeys(keyLookUp.get(2))
                .build();

        Config third = new ConfigBuilder()
                .withNodeId(nodeId)
                .withNodeNumbber(3)
                .withExecutionContext(executionContext)
                .withQt2Port(port.incrementAndGet())
                .withP2pPort(port.incrementAndGet())
                .withAdminPort(port.incrementAndGet())
                .withAlwaysSendTo(first.getKeys()
                        .getKeyData().stream()
                        .findFirst()
                        .get()
                        .getPublicKey())
                .withKeys(keyLookUp.get(3))
                .build();

        Config fourth = new ConfigBuilder()
                .withNodeId(nodeId)
                .withNodeNumbber(4)
                .withExecutionContext(executionContext)
                .withQt2Port(port.incrementAndGet())
                .withP2pPort(port.incrementAndGet())
                .withAdminPort(port.incrementAndGet())
                .withKeys(keyLookUp.get(4))
                .build();

        first.addPeer(new Peer(second.getP2PServerConfig().getServerAddress()));
        second.addPeer(new Peer(third.getP2PServerConfig().getServerAddress()));
        third.addPeer(new Peer(fourth.getP2PServerConfig().getServerAddress()));
        fourth.addPeer(new Peer(first.getP2PServerConfig().getServerAddress()));


        return Arrays.asList(first, second, third, fourth);
    }

    public static void main(String[] args) throws URISyntaxException {

        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        ExecutionContext executionContext = ExecutionContext.Builder
                .create()
                .with(CommunicationType.REST)
                .with(DBType.H2)
                .with(SocketType.UNIX)
                .build();

        Path path = new ConfigGenerator().calculatePath(executionContext);

        List<Config> configs = new ConfigGenerator().createConfigs(executionContext);

        for (int i = 1; i <= configs.size(); i++) {
            String filename = String.format("config%d.json", i);
            Path ouputFile = path.resolve(filename);
            System.out.println(ouputFile);
        }

    }

}
