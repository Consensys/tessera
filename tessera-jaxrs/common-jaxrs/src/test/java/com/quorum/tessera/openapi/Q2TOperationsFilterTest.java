package com.quorum.tessera.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.Operation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class Q2TOperationsFilterTest {

  private Q2TOperationsFilter operationsFilter;

  private Operation operation;

  private ApiDescription api;

  private Map<String, List<String>> params;

  private Map<String, String> cookies;

  private Map<String, List<String>> headers;

  @Before
  public void setUp() {
    operationsFilter = new Q2TOperationsFilter();
    operation = mock(Operation.class);
    api = mock(ApiDescription.class);
    params = Collections.emptyMap();
    cookies = Collections.emptyMap();
    headers = Collections.emptyMap();
  }

  @Test
  public void filerQ2TOperation() {
    when(operation.getTags()).thenReturn(List.of("quorum-to-tessera", "other-tag"));

    Optional<Operation> result =
        operationsFilter.filterOperation(operation, api, params, cookies, headers);

    assertThat(result).isPresent();
    assertThat(result).hasValue(operation);

    verify(operation).getTags();
  }

  @Test
  public void filerNonQ2TOperation() {
    when(operation.getTags()).thenReturn(List.of("not-quorum-to-tessera", "other-tag"));

    Optional<Operation> result =
        operationsFilter.filterOperation(operation, api, params, cookies, headers);

    assertThat(result).isNotPresent();

    verify(operation).getTags();
  }
}
