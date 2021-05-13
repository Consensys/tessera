package com.quorum.tessera.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class NodeUriTest {

  @Test
  public void createNormalisesStringValue() {

    String stringValue = "http://ilovesparrows.com";

    NodeUri nodeUri = NodeUri.create(stringValue);

    assertThat(nodeUri).isNotNull();
    assertThat(nodeUri.asString()).startsWith(stringValue).endsWith("/");
  }

  @Test
  public void createNormalisesUriValue() {
    String stringValue = "http://ilovesparrows.com";
    URI uriValue = URI.create(stringValue);

    NodeUri nodeUri = NodeUri.create(uriValue);

    assertThat(nodeUri).isNotNull();
    assertThat(nodeUri.asString()).startsWith(stringValue).endsWith("/");
    assertThat(nodeUri.toString()).isNotNull();
    assertThat(nodeUri.asURI()).isEqualTo(URI.create(stringValue.concat("/")));
  }

  @Test
  public void hashCodeAndEquals() {
    EqualsVerifier.forClass(NodeUri.class).usingGetClass().withNonnullFields("value").verify();
  }
}
