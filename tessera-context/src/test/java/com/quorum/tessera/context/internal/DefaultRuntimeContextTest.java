package com.quorum.tessera.context.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.PublicKey;
import jakarta.ws.rs.client.Client;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class DefaultRuntimeContextTest {

  @Test
  public void openPojoTest() {

    final Validator pojoValidator =
        ValidatorBuilder.create().with(new GetterMustExistRule()).with(new GetterTester()).build();

    pojoValidator.validate(PojoClassFactory.getPojoClass(DefaultRuntimeContext.class));
  }

  @Test
  public void testToString() {

    DefaultRuntimeContext instance =
        new DefaultRuntimeContext(
            Set.of(),
            mock(KeyEncryptor.class),
            List.of(),
            List.of(),
            mock(Client.class),
            true,
            true,
            mock(URI.class),
            true,
            true,
            true,
            true,
            true);

    assertThat(instance).isNotNull();
    assertThat(instance.toString()).isNotNull().isNotBlank();
  }

  @Test
  public void getPublicKeys() {

    PublicKey publicKey = mock(PublicKey.class);

    Set<PublicKey> keys = Set.of(publicKey);

    DefaultRuntimeContext instance =
        new DefaultRuntimeContext(
            keys,
            mock(KeyEncryptor.class),
            List.of(),
            List.of(),
            mock(Client.class),
            true,
            true,
            mock(URI.class),
            true,
            true,
            true,
            true,
            true);

    assertThat(instance.getPublicKeys()).containsExactly(publicKey);
  }
}
