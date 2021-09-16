package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class AzureVaultKeyPairValidator
    implements ConstraintValidator<ValidAzureVaultKeyPair, AzureVaultKeyPair> {

  @Override
  public boolean isValid(final AzureVaultKeyPair keypair, final ConstraintValidatorContext cvc) {
    if (keypair == null) {
      return true;
    }

    return Objects.isNull(keypair.getPublicKeyVersion())
        == Objects.isNull(keypair.getPrivateKeyVersion());
  }
}
