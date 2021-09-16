package com.quorum.tessera.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;

public class AbstractApiResourceTest {

  class DummyApiResource extends AbstractApiResource {

    @Override
    public String getOpenApiDocName() {
      return "dummy";
    }
  }

  private DummyApiResource apiResource;

  private Request request;

  @Before
  public void setUp() {
    apiResource = new DummyApiResource();
    request = mock(Request.class);
  }

  @Test
  public void getOpenApiDocName() {
    String result = apiResource.getOpenApiDocName();
    assertThat(result).isEqualTo("dummy");
  }

  @Test
  public void getResourceUrl() {
    URL result = apiResource.getResourceUrl("/dummy.yaml");
    assertThat(result).isNotNull();
  }

  @Test
  public void getVariants() {
    List<Variant> result = apiResource.getVariants();

    final Variant json = new Variant(new MediaType("application", "json"), (Locale) null, null);
    final Variant yaml = new Variant(new MediaType("application", "yaml"), (Locale) null, null);

    assertThat(result).containsExactlyInAnyOrder(json, yaml);
  }

  @Test
  public void api() throws Exception {
    final Variant variant = mock(Variant.class);
    final MediaType mediaType = mock(MediaType.class);
    final String subtype = "yaml";

    when(request.selectVariant(anyList())).thenReturn(variant);
    when(variant.getMediaType()).thenReturn(mediaType);
    when(mediaType.getSubtype()).thenReturn(subtype);

    final Response result = apiResource.api(request);

    assertThat(result.getEntity()).isNotNull();
    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getMediaType()).isEqualTo(mediaType);

    verify(request).selectVariant(apiResource.getVariants());
  }

  @Test
  public void apiUnsupportedMediaType() throws Exception {
    when(request.selectVariant(anyList())).thenReturn(null);

    final Response result = apiResource.api(request);

    assertThat(result.getStatus()).isEqualTo(400);

    verify(request).selectVariant(apiResource.getVariants());
  }
}
