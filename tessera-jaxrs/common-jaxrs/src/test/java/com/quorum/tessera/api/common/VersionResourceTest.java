package com.quorum.tessera.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.api.Version;
import com.quorum.tessera.version.ApiVersion;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VersionResourceTest {

  private VersionResource instance;

  private Version expectedVersion;

  private static final String VERSION_VALUE = "MOCK";

  @Before
  public void onSetUp() {
    expectedVersion = mock(Version.class);
    when(expectedVersion.version()).thenReturn(VERSION_VALUE);
    instance = new VersionResource(expectedVersion);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(expectedVersion);
  }

  @Test
  public void getVersion() {
    assertThat(instance.getVersion()).isEqualTo(VERSION_VALUE);
    verify(expectedVersion).version();
  }

  @Test
  public void getDistributionVersion() {
    assertThat(instance.getDistributionVersion()).isEqualTo(VERSION_VALUE);

    verify(expectedVersion).version();
  }

  @Test
  public void getVersions() {
    // Make sure that elements are defined in unnatural order to test sorting
    List<Double> versions = List.of(03.00, 01.00, 02.00);

    JsonArray result;
    try (var apiVersionMockedStatic = mockStatic(ApiVersion.class)) {

      apiVersionMockedStatic
          .when(ApiVersion::versions)
          .thenReturn(
              versions.stream()
                  .map(String::valueOf)
                  .map(s -> "v" + s)
                  .collect(Collectors.toList()));

      result = instance.getVersions();

      apiVersionMockedStatic.verify(ApiVersion::versions);
      apiVersionMockedStatic.verifyNoMoreInteractions();
    }

    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
    versions.stream().sorted().map(String::valueOf).forEach(v -> jsonArrayBuilder.add(v));
    JsonArray expected = jsonArrayBuilder.build();

    assertThat(result).containsExactlyElementsOf(expected);
  }

  @Test
  public void getVersionsNoPrefix() {
    // Make sure that elements are defined in unnatural order to test sorting
    List<Double> versions = List.of(03.00, 01.00, 02.00);

    JsonArray result;
    try (var apiVersionMockedStatic = mockStatic(ApiVersion.class)) {

      apiVersionMockedStatic
          .when(ApiVersion::versions)
          .thenReturn(versions.stream().map(String::valueOf).collect(Collectors.toList()));

      result = instance.getVersions();

      apiVersionMockedStatic.verify(ApiVersion::versions);
      apiVersionMockedStatic.verifyNoMoreInteractions();
    }

    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
    versions.stream().sorted().map(String::valueOf).forEach(v -> jsonArrayBuilder.add(v));
    JsonArray expected = jsonArrayBuilder.build();

    assertThat(result).containsExactlyElementsOf(expected);
  }

  @Test
  public void defaultConstructor() {
    VersionResource versionResource = new VersionResource();
    assertThat(versionResource).isNotNull();
    assertThat(versionResource.getDistributionVersion())
        .isEqualTo(System.getProperty("project.version"), "project.version not set");
    assertThat(versionResource.getVersion())
        .isEqualTo(System.getProperty("project.version"), "project.version not set");
  }
}
