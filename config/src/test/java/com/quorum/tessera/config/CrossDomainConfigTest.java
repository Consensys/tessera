package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.util.JaxbUtil;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.junit.Test;

public class CrossDomainConfigTest {

  @Test
  public void doStuff() {
    assertThat(JsonProvider.provider()).isNotNull();
  }

  @Test
  public void unmarshal() {
    byte[] json =
        "{\"allowedMethods\" : [\"GET\", \"OPTIONS\"], \"allowedOrigins\": [\"*\"], \"allowedHeaders\": [\"*\"], \"allowCredentials\": false}"
            .getBytes();

    CrossDomainConfig result =
        JaxbUtil.unmarshal(new ByteArrayInputStream(json), CrossDomainConfig.class);

    assertThat(result.getAllowedMethods()).containsExactly("GET", "OPTIONS");
    assertThat(result.getAllowedOrigins()).containsExactly("*");
    assertThat(result.getAllowedHeaders()).containsExactly("*");
    assertThat(result.getAllowCredentials()).isFalse();
  }

  @Test
  public void unmarshalDefaults() {
    byte[] json = "{}".getBytes();

    CrossDomainConfig result =
        JaxbUtil.unmarshal(new ByteArrayInputStream(json), CrossDomainConfig.class);

    assertThat(result.getAllowedMethods())
        .containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD");
    assertThat(result.getAllowedOrigins()).isNull();
    assertThat(result.getAllowedHeaders()).isNull();
    assertThat(result.getAllowCredentials()).isTrue();
  }

  @Test
  public void marshal() {
    CrossDomainConfig config = new CrossDomainConfig();
    config.setAllowedMethods(Arrays.asList("GET", "OPTIONS"));
    config.setAllowedOrigins(Arrays.asList("a", "b"));
    config.setAllowedHeaders(Arrays.asList("A", "B"));
    config.setAllowCredentials(Boolean.TRUE);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JaxbUtil.marshal(config, out);

    JsonObject result = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();

    assertThat(result.getJsonArray("allowedMethods").getValuesAs(JsonString.class))
        .containsExactlyInAnyOrder(Json.createValue("GET"), Json.createValue("OPTIONS"));
    assertThat(result.getJsonArray("allowedOrigins").getValuesAs(JsonString.class))
        .containsExactlyInAnyOrder(Json.createValue("a"), Json.createValue("b"));
    assertThat(result.getJsonArray("allowedHeaders").getValuesAs(JsonString.class))
        .containsExactlyInAnyOrder(Json.createValue("A"), Json.createValue("B"));
    assertThat(result.get("allowCredentials")).isEqualTo(JsonValue.TRUE);
  }

  @Test
  public void marshalEmpty() {
    CrossDomainConfig config = new CrossDomainConfig();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JaxbUtil.marshal(config, out);

    JsonObject result = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();

    assertThat(result.getJsonArray("allowedMethods")).isNull();
    assertThat(result.getJsonArray("allowedOrigins")).isNull();
    assertThat(result.getJsonArray("allowedHeaders")).isNull();
    assertThat(result.get("allowCredentials")).isNull();
  }
}
