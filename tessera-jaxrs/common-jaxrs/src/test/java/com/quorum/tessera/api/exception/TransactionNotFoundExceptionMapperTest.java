package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class TransactionNotFoundExceptionMapperTest {

  private TransactionNotFoundExceptionMapper instance = new TransactionNotFoundExceptionMapper();

  @Test
  public void toResponse() {
    final TransactionNotFoundException transactionNotFoundException =
        new TransactionNotFoundException("OUCH");

    final Response result = instance.toResponse(transactionNotFoundException);
    assertThat(result).isNotNull();

    final String message = result.getEntity().toString();

    assertThat(message).isEqualTo("OUCH");
    assertThat(result.getStatus()).isEqualTo(404);
  }
}
