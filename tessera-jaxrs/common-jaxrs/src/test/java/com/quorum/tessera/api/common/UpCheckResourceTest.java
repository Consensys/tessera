package com.quorum.tessera.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.transaction.TransactionManager;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UpCheckResourceTest {

  private UpCheckResource resource;

  private TransactionManager transactionManager;

  @Before
  public void onSetup() {
    this.transactionManager = mock(TransactionManager.class);

    this.resource = new UpCheckResource(transactionManager);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(transactionManager);
  }

  @Test
  public void upcheck() {
    when(transactionManager.upcheck()).thenReturn(true);

    final Response response = resource.upCheck();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isEqualTo("I'm up!");

    verify(transactionManager).upcheck();
  }

  @Test
  public void upcheckWhenDBNotReady() {
    when(transactionManager.upcheck()).thenReturn(false);

    final Response response = resource.upCheck();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isEqualTo("Database unavailable");

    verify(transactionManager).upcheck();
  }
}
