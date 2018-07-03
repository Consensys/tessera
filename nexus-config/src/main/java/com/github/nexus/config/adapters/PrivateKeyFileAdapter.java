package com.github.nexus.config.adapters;

import com.github.nexus.config.PrivateKey;
import com.github.nexus.config.PrivateKeyConfig;
import com.github.nexus.config.PrivateKeyType;
import com.github.nexus.config.util.JaxbUtil;
import com.github.nexus.keyenc.KeyConfig;
import com.github.nexus.keyenc.KeyEncryptor;
import com.github.nexus.keyenc.KeyEncryptorFactory;
import java.io.IOException;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PrivateKeyFileAdapter extends XmlAdapter<Element, PrivateKey> {

    private final KeyEncryptor keyEncryptor = KeyEncryptorFactory.create();

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    private PrivateKeyConfig fromPath(Path path) throws IOException {

        try (InputStream inputStream = Files.newInputStream(path)) {
            return JaxbUtil.unmarshal(inputStream, PrivateKeyConfig.class);
        }
    }

    /*
        Workaround to readd namespace lost when converting into json. 
     */
    static Node addNamespace(Node node) throws TransformerException, IOException {
        URL url = JaxbUtil.class.getResource("/xsl/decorate-namespace.xsl");
        Transformer transformer
                = TransformerFactory.newInstance()
                        .newTransformer(new StreamSource(url.openStream()));

        DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(node), result);
        return result.getNode();

    }

    @Override
    public PrivateKey unmarshal(Element privateKey) throws Exception {

        String value = xpath.evaluate("value", privateKey);

        String pathString = xpath.evaluate("path", privateKey);
        Path path;
        if (!"".equals(pathString)) {
            path = Paths.get(pathString);
        } else {
            path = null;
        }

        Node configNode = (Node) xpath.evaluate("config", privateKey, XPathConstants.NODE);

        final PrivateKeyConfig config;

        if (Objects.nonNull(path)) {
            config = fromPath(path);
        } else if (Objects.nonNull(configNode)) {
            Node n = addNamespace(configNode);
            config = JAXB.unmarshal(new DOMSource(n), PrivateKeyConfig.class);
        } else {
            return new PrivateKey(null, value, null);
        }

        final String encryptedKey;
        if (config.getType() == PrivateKeyType.UNLOCKED) {
            encryptedKey = config.getPrivateKeyData().getValue();
        } else {

            KeyConfig keyConfig = KeyConfig.Builder.create()
                    .argonAlgorithm(config.getArgonOptions().getAlgorithm())
                    .argonIterations(config.getArgonOptions().getIterations())
                    .argonMemory(config.getArgonOptions().getMemory())
                    .argonParallelism(config.getArgonOptions().getParallelism())
                    .asalt(toBytes(config.getAsalt()))
                    .password(config.getPassword())
                    .snonce(toBytes(config.getSnonce()))
                    .sbox(toBytes(config.getSbox()))
                    .build();

            byte[] data = keyEncryptor.decryptPrivateKey(keyConfig).getKeyBytes();
            encryptedKey = Base64.getEncoder().encodeToString(data);

        }

        return new PrivateKey(path, encryptedKey, config);
    }

    static byte[] toBytes(String str) {
        return Optional.ofNullable(str).map(String::getBytes).orElse(null);
    }

    @Override
    public Element marshal(PrivateKey v) throws Exception {
        DOMResult result = new DOMResult();
        JAXB.marshal(v, result);
        return Document.class.cast(result.getNode()).getDocumentElement();
    }

}
