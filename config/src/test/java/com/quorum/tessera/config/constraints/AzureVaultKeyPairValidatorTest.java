package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureVaultKeyPairValidatorTest {

    private AzureVaultKeyPairValidator validator;

    private AzureVaultKeyPair keyPair;

    private ConstraintValidatorContext cvc;

    @Before
    public void setUp() {
        this.validator = new AzureVaultKeyPairValidator();
        this.keyPair = mock(AzureVaultKeyPair.class);
        this.cvc = mock(ConstraintValidatorContext.class);
    }

    @Test
    public void nullKeyPairIsValid() {
        assertThat(validator.isValid(null, cvc)).isTrue();
    }

    @Test
    public void publicAndPrivateKeyVersionsNotSetIsValid() {
        when(keyPair.getPublicKeyVersion()).thenReturn(null);
        when(keyPair.getPrivateKeyVersion()).thenReturn(null);

        assertThat(validator.isValid(keyPair, cvc)).isTrue();
    }

    @Test
    public void publicAndPrivateKeyVersionsAreSetIsValid() {
        when(keyPair.getPublicKeyVersion()).thenReturn("pubVer");
        when(keyPair.getPrivateKeyVersion()).thenReturn("privVer");

        assertThat(validator.isValid(keyPair, cvc)).isTrue();
    }

    @Test
    public void onlyPublicKeyVersionSetIsNotValid() {
        when(keyPair.getPublicKeyVersion()).thenReturn("pubVer");
        when(keyPair.getPrivateKeyVersion()).thenReturn(null);

        assertThat(validator.isValid(keyPair, cvc)).isFalse();
    }

    @Test
    public void onlyPrivateKeyVersionSetIsNotValid() {
        when(keyPair.getPublicKeyVersion()).thenReturn(null);
        when(keyPair.getPrivateKeyVersion()).thenReturn("privVer");

        assertThat(validator.isValid(keyPair, cvc)).isFalse();
    }
}
