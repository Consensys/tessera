package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.util.JaxbUtil;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class EncryptorConfigTest {

  @Test
  public void twoOfSameTypeAndEmptyProperteisAreEqual() {

    EncryptorConfig encryptorConfig = new EncryptorConfig();
    encryptorConfig.setType(EncryptorType.NACL);

    EncryptorConfig otherEncryptorConfig = new EncryptorConfig();
    otherEncryptorConfig.setType(EncryptorType.NACL);

    assertThat(encryptorConfig).isEqualTo(otherEncryptorConfig);
    assertThat(encryptorConfig).isEqualTo(encryptorConfig);
    assertThat(encryptorConfig).isNotEqualTo(new HashMap());
  }

  @Test
  public void notEqualsNull() {

    EncryptorConfig encryptorConfig = new EncryptorConfig();
    encryptorConfig.setType(EncryptorType.NACL);

    assertThat(encryptorConfig).isNotEqualTo(null);
  }

  @Test
  public void differentTypesNotEqual() {

    EncryptorConfig encryptorConfig = new EncryptorConfig();
    encryptorConfig.setType(EncryptorType.NACL);

    EncryptorConfig otherEncryptorConfig = new EncryptorConfig();
    otherEncryptorConfig.setType(EncryptorType.EC);

    assertThat(encryptorConfig).isNotEqualTo(otherEncryptorConfig);
  }

  @Test
  public void twoOfSameTypeAndDifferntPropertiesAreNotEqual() {

    EncryptorConfig encryptorConfig = new EncryptorConfig();
    encryptorConfig.setType(EncryptorType.NACL);

    EncryptorConfig otherEncryptorConfig = new EncryptorConfig();
    otherEncryptorConfig.setType(EncryptorType.NACL);

    Map<String, String> props = new HashMap<>();
    props.put("foo", "bar");

    otherEncryptorConfig.setProperties(props);

    assertThat(encryptorConfig).isNotEqualTo(otherEncryptorConfig);
  }

  @Test
  public void unmarshal() {

    JsonObject json =
        Json.createObjectBuilder()
            .add("type", "EC")
            .add(
                "properties",
                Json.createObjectBuilder()
                    .add("greeting", "Hellow")
                    .add("something", "ELSE")
                    .addNull("bogus"))
            .build();

    String data = json.toString();

    EncryptorConfig result =
        JaxbUtil.unmarshal(new ByteArrayInputStream(data.getBytes()), EncryptorConfig.class);

    assertThat(result.getProperties()).containsKeys("greeting", "something", "bogus");
    assertThat(result.getProperties().get("greeting")).isEqualTo("Hellow");
    assertThat(result.getProperties().get("something")).isEqualTo("ELSE");
    assertThat(result.getProperties().get("bogus")).isNull();
  }

  @Test
  public void marshal() throws ClassNotFoundException {

    EncryptorConfig encryptorConfig = new EncryptorConfig();
    encryptorConfig.setType(EncryptorType.EC);
    Map<String, String> properties = new HashMap<>();
    properties.put("greeting", "Hellow");
    properties.put("something", "ELSE");
    properties.put("bogus", null);

    encryptorConfig.setProperties(properties);
    // JaxbUtil.marshal(encryptorConfig, System.out);
    String result = JaxbUtil.marshalToStringNoValidation(encryptorConfig);

    JsonObject json = Json.createReader(new StringReader(result)).readObject();

    assertThat(json.getJsonObject("properties")).containsKeys("greeting", "something", "bogus");
    assertThat(json.getJsonObject("properties").getString("greeting")).isEqualTo("Hellow");
    assertThat(json.getJsonObject("properties").getString("something")).isEqualTo("ELSE");
  }

  @Test
  public void getDefault() {
    EncryptorConfig encryptorConfig = EncryptorConfig.getDefault();
    assertThat(encryptorConfig.getType()).isEqualTo(EncryptorType.NACL);
    assertThat(encryptorConfig.getProperties()).isNull();
  }

  @Test
  public void marshalCUSTOM() {

    EncryptorConfig encryptorConfig = new EncryptorConfig();
    encryptorConfig.setType(EncryptorType.CUSTOM);
    Map<String, String> properties = new HashMap<>();
    properties.put("greeting", "Hellow");
    properties.put("something", "ELSE");
    properties.put("bogus", null);

    encryptorConfig.setProperties(properties);

    String result = JaxbUtil.marshalToStringNoValidation(encryptorConfig);

    JsonObject json = Json.createReader(new StringReader(result)).readObject();

    assertThat(json.getJsonObject("properties")).containsKeys("greeting", "something", "bogus");
    assertThat(json.getJsonObject("properties").getString("greeting")).isEqualTo("Hellow");
    assertThat(json.getJsonObject("properties").getString("something")).isEqualTo("ELSE");
  }
}
