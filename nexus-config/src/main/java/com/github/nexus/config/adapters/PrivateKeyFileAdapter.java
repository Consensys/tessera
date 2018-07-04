package com.github.nexus.config.adapters;

import com.github.nexus.config.PrivateKey;
import com.github.nexus.config.util.JaxbUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrivateKeyFileAdapter extends XmlAdapter<Element, PrivateKey> {

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    @Override
    public PrivateKey unmarshal(Element privateKeyElement) throws Exception {

        //FIXME : Is this being used?
        XPathExpression hasDefinedPath = xpath.compile("not(count(path) = 0)");

        XPathExpression hasTypeAttribute = xpath.compile("count(@type) = 1");

        if (Boolean.valueOf(hasTypeAttribute.evaluate(privateKeyElement))) {

            // FIXME: Need to workout why we lose the xmlns
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer(
                    new StreamSource(getClass().getResource("/xsl/decorate-namespace.xsl").openStream()));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

                transformer.transform(new DOMSource(privateKeyElement), new StreamResult(output));
                output.flush();

                try (InputStream in = new ByteArrayInputStream(output.toByteArray())) {
                    return JAXB.unmarshal(in, PrivateKey.class);
                }
            }
        }

        String pathString = xpath.evaluate(".", privateKeyElement);

        Path path = Paths.get(pathString);

        try (InputStream inputStream = Files.newInputStream(path)) {
            return JaxbUtil.unmarshal(inputStream, PrivateKey.class);
        }

    }

    //FIXME: For this to work properly some more ns work is required
    @Override
    public Element marshal(PrivateKey v) {
        DOMResult dOMResult = new DOMResult();
        JAXB.marshal(v, dOMResult);
        return ((Document) dOMResult.getNode()).getDocumentElement();

    }

}
