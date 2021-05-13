package com.quorum.tessera.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.encryption.PublicKey;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ActiveNodeTest {

  private static final String URI = "http://bogus.com";

  @Test
  public void createOnlyWithUri() {
    final NodeUri nodeUri = NodeUri.create(URI);

    final ActiveNode activeNode = ActiveNode.Builder.create().withUri(nodeUri).build();

    assertThat(activeNode).isNotNull();
    assertThat(activeNode.getKeys()).isEmpty();
    assertThat(activeNode.getSupportedVersions()).isEmpty();
    assertThat(activeNode.getUri()).isSameAs(nodeUri);
  }

  @Test
  public void createOnlyWithKeys() {

    final NodeUri nodeUri = NodeUri.create(URI);
    final PublicKey publicKey = mock(PublicKey.class);

    final List<PublicKey> keys = List.of(publicKey);

    final ActiveNode activeNode =
        ActiveNode.Builder.create().withUri(nodeUri).withKeys(keys).build();

    assertThat(activeNode).isNotNull();
    assertThat(activeNode.getKeys()).containsExactly(publicKey);
    assertThat(activeNode.getSupportedVersions()).isEmpty();
    assertThat(activeNode.getUri()).isSameAs(nodeUri);
  }

  @Test
  public void createWithEverything() {

    final NodeUri nodeUri = NodeUri.create(URI);
    final PublicKey publicKey = mock(PublicKey.class);
    final List<String> supportedVersions = List.of("One", "Two");
    final List<PublicKey> keys = List.of(publicKey);

    final ActiveNode activeNode =
        ActiveNode.Builder.create()
            .withUri(nodeUri)
            .withKeys(keys)
            .withSupportedVersions(supportedVersions)
            .build();

    assertThat(activeNode).isNotNull();
    assertThat(activeNode.getKeys()).containsExactly(publicKey);
    assertThat(activeNode.getSupportedVersions())
        .containsExactlyInAnyOrderElementsOf(supportedVersions);
    assertThat(activeNode.getUri()).isSameAs(nodeUri);
  }

  @Test
  public void hashCodeAndEquals() {
    EqualsVerifier.forClass(ActiveNode.class)
        .usingGetClass()
        .withNonnullFields("uri")
        .withIgnoredFields("supportedVersions", "keys")
        .verify();
  }

  @Test
  public void createFrom() {

    final NodeUri nodeUri = NodeUri.create(URI);
    final PublicKey publicKey = mock(PublicKey.class);
    final List<String> supportedVersions = List.of("One", "Two");
    final List<PublicKey> keys = List.of(publicKey);

    final ActiveNode activeNode =
        ActiveNode.Builder.create()
            .withUri(nodeUri)
            .withKeys(keys)
            .withSupportedVersions(supportedVersions)
            .build();

    ActiveNode result = ActiveNode.Builder.from(activeNode).build();

    assertThat(result).isNotNull();
    assertThat(result.getKeys()).containsExactly(publicKey);
    assertThat(result.getSupportedVersions())
        .containsExactlyInAnyOrderElementsOf(supportedVersions);
    assertThat(result.getUri()).isSameAs(nodeUri);
    assertThat(result.toString()).contains(URI);
  }
}
