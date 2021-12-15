package com.quorum.tessera.api.constraint;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendSignedRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RequestPrivacyValidatorTest {

  private RequestPrivacyValidator validator = new RequestPrivacyValidator();

  private ConstraintValidatorContext context;

  private ConstraintValidatorContext.ConstraintViolationBuilder builder;

  @Before
  public void init() {
    context = mock(ConstraintValidatorContext.class);
    builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    when(builder.addConstraintViolation()).thenReturn(context);
    when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(context);
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void testPrivacyValidationOnSendRequest() {

    SendRequest request = new SendRequest();
    request.setPrivacyFlag(-1000);
    assertThat(validator.isValid(request, context)).isTrue();

    request.setPrivacyFlag(0);
    assertThat(validator.isValid(request, context)).isTrue();

    request.setPrivacyFlag(1);
    assertThat(validator.isValid(request, context)).isTrue();

    request.setPrivacyFlag(2);
    assertThat(validator.isValid(request, context)).isTrue();

    request.setPrivacyFlag(3);
    assertThat(validator.isValid(request, context)).isFalse();
    verify(context).buildConstraintViolationWithTemplate("Exec hash missing");
    verify(builder).addConstraintViolation();

    request.setExecHash("execHash");
    assertThat(validator.isValid(request, context)).isTrue();
  }

  @Test
  public void testPrivacyValidationOnSendSignedRequest() {

    SendSignedRequest request = new SendSignedRequest();
    request.setPrivacyFlag(-1000);
    assertThat(validator.isValid(request, context)).isTrue();

    request.setPrivacyFlag(0);
    assertThat(validator.isValid(request, context)).isTrue();

    request.setPrivacyFlag(1);
    assertThat(validator.isValid(request, context)).isTrue();

    request.setPrivacyFlag(2);
    assertThat(validator.isValid(request, context)).isTrue();

    request.setPrivacyFlag(3);
    assertThat(validator.isValid(request, context)).isFalse();
    verify(context).buildConstraintViolationWithTemplate("Exec hash missing");
    verify(builder).addConstraintViolation();

    request.setExecHash("execHash");
    assertThat(validator.isValid(request, context)).isTrue();
  }

  @Test
  public void testWrongUsage() {
    Object someObject = new Object();
    assertThat(validator.isValid(someObject, context)).isFalse();
    verify(context)
        .buildConstraintViolationWithTemplate(
            "Invalid usage. This validator can only be apply to SendRequest or SendSignedRequest");
    verify(builder).addConstraintViolation();
  }
}
