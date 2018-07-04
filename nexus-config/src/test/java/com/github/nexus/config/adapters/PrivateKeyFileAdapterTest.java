//package com.github.nexus.config.adapters;
//
//import com.github.nexus.config.PrivateKey;
//import com.github.nexus.config.PrivateKeyType;
//import java.io.ByteArrayInputStream;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import javax.xml.parsers.DocumentBuilderFactory;
//import static org.assertj.core.api.Assertions.assertThat;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//
//public class PrivateKeyFileAdapterTest {
//
//    private PrivateKeyFileAdapter privateKeyFileAdapter;
//
//    public PrivateKeyFileAdapterTest() {
//    }
//
//    @Before
//    public void setUp() {
//        this.privateKeyFileAdapter = new PrivateKeyFileAdapter();
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//    @Test
//    public void unmarshalLocked() throws Exception {
//
//        Path privateKeyFile = Paths.get(getClass().getResource("/lockedprivatekey.json").toURI());
//
//        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
//                + "<privateKey xmlns=\"http://nexus.github.com/config\">\n"
//                + "   <path>"
//                + "" + privateKeyFile
//                + "</path>\n"
//                + "   <value>6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=</value>\n"
//                + "   <config type=\"argon2sbox\">\n"
//                + "      <data>\n"
//                + "         <snonce>x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC</snonce>\n"
//                + "         <asalt>7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=</asalt>\n"
//                + "         <sbox>d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc</sbox>\n"
//                + "         <aopts variant=\"id\" iterations=\"10\" memory=\"1048576\" parallelism=\"4\"/>\n"
//                + "         <password>q</password>\n"
//                + "      </data>\n"
//                + "   </config>\n"
//                + "</privateKey>";
//
//        Document doc = DocumentBuilderFactory.newInstance()
//                .newDocumentBuilder()
//                .parse(new ByteArrayInputStream(xml.getBytes()));
//
//        PrivateKey privateKey = privateKeyFileAdapter.unmarshal(doc.getDocumentElement());
//
//        assertThat(privateKey).isNotNull();
//        assertThat(privateKey.getValue()).isNotNull();
//        assertThat(privateKey.getPath()).isEqualTo(privateKeyFile);
//        assertThat(privateKey.getConfig()).isNotNull();
//
//        assertThat(privateKey.getConfig().getType()).isEqualTo(PrivateKeyType.LOCKED);
//        assertThat(privateKey.getConfig().getPrivateKeyData()).isNotNull();
//
//        assertThat(privateKey.getConfig().getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
//        assertThat(privateKey.getConfig().getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
//        assertThat(privateKey.getConfig().getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");
//
//        assertThat(privateKey.getConfig().getArgonOptions()).isNotNull();
//        assertThat(privateKey.getConfig().getArgonOptions().getAlgorithm()).isEqualTo("id");
//        assertThat(privateKey.getConfig().getArgonOptions().getIterations()).isEqualTo(10);
//        assertThat(privateKey.getConfig().getArgonOptions().getParallelism()).isEqualTo(4);
//        assertThat(privateKey.getConfig().getArgonOptions().getMemory()).isEqualTo(1048576);
//    }
//
//    @Test
//    public void unmarshalValueOnly() throws Exception {
//
//        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
//                + "<privateKey xmlns=\"http://nexus.github.com/config\">\n"
//                + "   <value>SOMEVALUE</value>\n"
//                + "</privateKey>";
//
//        Document doc = DocumentBuilderFactory.newInstance()
//                .newDocumentBuilder()
//                .parse(new ByteArrayInputStream(xml.getBytes()));
//
//        PrivateKey privateKey = privateKeyFileAdapter.unmarshal(doc.getDocumentElement());
//
//        assertThat(privateKey).isNotNull();
//        assertThat(privateKey.getValue()).isEqualTo("SOMEVALUE");
//        assertThat(privateKey.getPath()).isNull();
//        assertThat(privateKey.getConfig()).isNull();
//
//    }
//
//    @Test
//    public void marshal() throws Exception {
//        PrivateKey privateKey = new PrivateKey(null, null, null);
//        Element result = privateKeyFileAdapter.marshal(privateKey);
//
//        assertThat(result).isNotNull();
//
//    }
//
//    @Test
//    public void unmarshalUnlocked() throws Exception {
//
//        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
//                + "<privateKey xmlns=\"http://nexus.github.com/config\">\n"
//                + "   <config type=\"unlocked\">\n"
//                + "    <data>"
//                + "     <bytes>6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=</bytes>"
//                + "    </data>"
//                + "   </config>\n"
//                + "</privateKey>";
//
//        Document doc = DocumentBuilderFactory.newInstance()
//                .newDocumentBuilder()
//                .parse(new ByteArrayInputStream(xml.getBytes()));
//
//        PrivateKey privateKey = privateKeyFileAdapter.unmarshal(doc.getDocumentElement());
//
//        assertThat(privateKey).isNotNull();
//        assertThat(privateKey.getValue()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");
//        assertThat(privateKey.getPath()).isNull();
//        assertThat(privateKey.getConfig()).isNotNull();
//        assertThat(privateKey.getConfig().getType()).isEqualTo(PrivateKeyType.UNLOCKED);
//        assertThat(privateKey.getConfig().getValue()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");
//        assertThat(privateKey.getConfig().getPrivateKeyData().getValue()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");
//    }
//}
