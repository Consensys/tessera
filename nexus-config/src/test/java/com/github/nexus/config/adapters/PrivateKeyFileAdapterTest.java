package com.github.nexus.config.adapters;

import com.github.nexus.config.PrivateKey;
import com.github.nexus.config.PrivateKeyType;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PrivateKeyFileAdapterTest {

    private PrivateKeyFileAdapter privateKeyFileAdapter;

    public PrivateKeyFileAdapterTest() {
    }

    @Before
    public void setUp() {
        this.privateKeyFileAdapter = new PrivateKeyFileAdapter();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void unmarshalLocked() throws Exception {

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<privateKey type=\"argon2sbox\">\n"
                + "<data>\n"
                + "<aopts iterations=\"10\" memory=\"1048576\" parallelism=\"4\" variant=\"id\" version=\"1.3\">\n"
                + "<variant>id</variant>\n"
                + "<memory>1048576</memory>\n"
                + "<iterations>10</iterations>\n"
                + "<parallelism>4</parallelism>\n"
                + "</aopts>\n"
                + "<snonce>x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC</snonce>\n"
                + "<asalt>7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=</asalt>\n"
                + "<sbox>d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc</sbox>\n"
                + "</data>\n"
                + "<type>argon2sbox</type>\n"
                + "</privateKey>";

        Document doc = DocumentBuilderFactory
                .newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));

        PrivateKey privateKey = privateKeyFileAdapter.unmarshal(doc.getDocumentElement());
        assertThat(privateKey).isNotNull();
        assertThat(privateKey.getValue())
                .describedAs("Value is poupulated once its called back to encryotion service").isNull();
        assertThat(privateKey.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(privateKey.getPrivateKeyData()).isNotNull();

        assertThat(privateKey.getPrivateKeyData().getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(privateKey.getPrivateKeyData().getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(privateKey.getPrivateKeyData().getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");

        assertThat(privateKey.getPrivateKeyData().getArgonOptions()).isNotNull();
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getMemory()).isEqualTo(1048576);
    }

    @Test
    public void unmarshalUnlocked() throws Exception {
        String path = getClass().getResource("/unlockedprivatekey.json").getPath();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<privateKey>"
                + path
                + "</privateKey>";

        Document doc = DocumentBuilderFactory
                .newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));

        PrivateKey privateKey = privateKeyFileAdapter.unmarshal(doc.getDocumentElement());
        assertThat(privateKey).isNotNull();
        assertThat(privateKey.getType()).isEqualTo(PrivateKeyType.UNLOCKED);
        assertThat(privateKey.getValue()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");

        assertThat(privateKey.getPrivateKeyData()).isNotNull();

        assertThat(privateKey.getPrivateKeyData().getSnonce()).isNull();
        assertThat(privateKey.getPrivateKeyData().getAsalt()).isNull();
        assertThat(privateKey.getPrivateKeyData().getSbox()).isNull();

        assertThat(privateKey.getPrivateKeyData().getArgonOptions()).isNull();

    }

    @Test
    public void marshalUnlocked() throws Exception {
        PrivateKey privateKey = new PrivateKey(null, PrivateKeyType.UNLOCKED);

        Element result = privateKeyFileAdapter.marshal(privateKey);

        XPath xpath = XPathFactory.newInstance().newXPath();

        assertThat(xpath.evaluate("@type", result)).isEqualTo("unlocked");

    }

    @Test
    public void marshalLocked() throws Exception {
        PrivateKey privateKey = new PrivateKey(null, PrivateKeyType.LOCKED);

        Element result = privateKeyFileAdapter.marshal(privateKey);

        XPath xpath = XPathFactory.newInstance().newXPath();

        assertThat(xpath.evaluate("@type", result)).isEqualTo("argon2sbox");

    }
}
