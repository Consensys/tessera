package config;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import suite.EnclaveType;
import suite.ExecutionContext;
import suite.NodeAlias;
import suite.NodeId;
import suite.SocketType;

public class ConfigGenerator {

    public List<ConfigDescriptor> generateConfigs(ExecutionContext executionContext) {

        Path path = calculatePath(executionContext);
        try{
            Files.createDirectories(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        List<Config> configs = createConfigs(executionContext);

        List<Config> enclaveConfigs;
        if (executionContext.getEnclaveType() == EnclaveType.REMOTE) {
            enclaveConfigs = configs.stream().map(this::createEnclaveConfig)
                    .collect(Collectors.toList());
            configs.forEach(c -> c.setKeys(null));
        } else {
            enclaveConfigs = Collections.EMPTY_LIST;
        }
        //Remove keys
        List<ConfigDescriptor> configList = new ArrayList<>();

        for (NodeAlias alias : NodeAlias.values()) {
            int i = alias.ordinal();
            final Config config = configs.get(i);

            final String filename = String.format("config%d.json", (i + 1));
            final Path ouputFile = path.resolve(filename);

            try (OutputStream out = Files.newOutputStream(ouputFile)){
                JaxbUtil.marshalWithNoValidation(config, out);

            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

            final Config enclaveConfig;
            final Path enclaveOuputFile;
            if (!enclaveConfigs.isEmpty()) {
                enclaveConfig = enclaveConfigs.get(i);
                String enclaveFilename = String.format("enclave%d.json", (i + 1));
                enclaveOuputFile = path.resolve(enclaveFilename);
                try (OutputStream enclaveout = Files.newOutputStream(enclaveOuputFile)){
                    JaxbUtil.marshalWithNoValidation(enclaveConfig, enclaveout);

                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            } else {
                enclaveConfig = null;
                enclaveOuputFile = null;
            }

            configList.add(new ConfigDescriptor(alias, ouputFile, config, enclaveConfig, enclaveOuputFile));
        }

        return configList;

    }

    Config createEnclaveConfig(Config config) {

        final Config enclaveConfig = new Config();

        ServerConfig serverConfig = config.getServerConfigs().stream()
                .filter(s -> s.getApp() == AppType.ENCLAVE)
                .findAny()
                .get();

        enclaveConfig.setServerConfigs(Arrays.asList(serverConfig));

        enclaveConfig.setKeys(config.getKeys());
        enclaveConfig.setAlwaysSendTo(config.getAlwaysSendTo());

        return enclaveConfig;

    }

    public static Path calculatePath(ExecutionContext executionContext) {
        try{
            URI baseUri = ConfigGenerator.class.getResource("/").toURI();
            return Paths.get(baseUri)
                    .resolve(executionContext.getCommunicationType().name().toLowerCase())
                    .resolve(executionContext.getSocketType().name().toLowerCase())
                    .resolve(executionContext.getDbType().name().toLowerCase())
                    .resolve("enclave-" + executionContext.getEnclaveType().name().toLowerCase());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Map<Integer, SortedMap<String, String>> keyLookUp = new HashMap<Integer, SortedMap<String, String>>() {
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

        PortUtil port = new PortUtil(50520);
        String nodeId = NodeId.generate(executionContext);

        Config first = new ConfigBuilder()
                .withNodeId(nodeId)
                .withNodeNumbber(1)
                .withExecutionContext(executionContext)
                .withQt2Port(port.nextPort())
                .withP2pPort(port.nextPort())
                .withAdminPort(port.nextPort())
                .withEnclavePort(port.nextPort())
                .withKeys(keyLookUp.get(1))
                .build();

        Config second = new ConfigBuilder()
                .withNodeId(nodeId)
                .withNodeNumbber(2)
                .withExecutionContext(executionContext)
                .withQt2Port(port.nextPort())
                .withP2pPort(port.nextPort())
                .withAdminPort(port.nextPort())
                .withEnclavePort(port.nextPort())
                .withKeys(keyLookUp.get(2))
                .build();

        Config third = new ConfigBuilder()
                .withNodeId(nodeId)
                .withNodeNumbber(3)
                .withExecutionContext(executionContext)
                .withQt2Port(port.nextPort())
                .withP2pPort(port.nextPort())
                .withAdminPort(port.nextPort())
                .withEnclavePort(port.nextPort())
                .withAlwaysSendTo(keyLookUp.get(1).keySet().iterator().next())
                .withKeys(keyLookUp.get(3))
                .build();

        Config fourth = new ConfigBuilder()
                .withNodeId(nodeId)
                .withNodeNumbber(4)
                .withExecutionContext(executionContext)
                .withQt2Port(port.nextPort())
                .withP2pPort(port.nextPort())
                .withAdminPort(port.nextPort())
                .withEnclavePort(port.nextPort())
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
                .with(SocketType.UNIX).with(EnclaveType.REMOTE)
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
