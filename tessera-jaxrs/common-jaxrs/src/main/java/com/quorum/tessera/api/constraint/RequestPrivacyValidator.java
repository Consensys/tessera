package com.quorum.tessera.api.constraint;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendSignedRequest;
import com.quorum.tessera.enclave.PrivacyMode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class RequestPrivacyValidator implements ConstraintValidator<PrivacyValid, Object> {

  @Override
  public boolean isValid(Object request, ConstraintValidatorContext context) {
    PrivacyMode privacyMode;
    String execHash;
    if (request instanceof SendRequest) {
      privacyMode = PrivacyMode.fromFlag(((SendRequest) request).getPrivacyFlag());
      execHash = ((SendRequest) request).getExecHash();
    } else if (request instanceof SendSignedRequest) {
      privacyMode = PrivacyMode.fromFlag(((SendSignedRequest) request).getPrivacyFlag());
      execHash = ((SendSignedRequest) request).getExecHash();
    } else {
      context
          .buildConstraintViolationWithTemplate(
              "Invalid usage. This validator can only be apply to SendRequest or SendSignedRequest")
          .addConstraintViolation();
      return false;
    }
    if (PrivacyMode.PRIVATE_STATE_VALIDATION == privacyMode) {
      if (Objects.isNull(execHash) || execHash.length() == 0) {
        context.buildConstraintViolationWithTemplate("Exec hash missing").addConstraintViolation();
        return false;
      }
    }
    return true;
  }
}
