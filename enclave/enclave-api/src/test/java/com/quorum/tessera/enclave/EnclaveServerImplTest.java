package com.quorum.tessera.enclave;

import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class EnclaveServerImplTest {

  @Test
  public void enclaveServerImplCallsDelegae() throws Exception {
    Enclave enclave = mock(Enclave.class);
    EnclaveServerImpl enclaveServer = new EnclaveServerImpl(enclave);

    List<Method> targetMethods =
        Stream.of(Enclave.class.getMethods())
            .filter(m -> !Modifier.isStatic(m.getModifiers()))
            .collect(Collectors.toList());

    for (Method targetMethod : targetMethods) {
      Class<?>[] parameterTypes = targetMethod.getParameterTypes();
      Object[] arguments = new Object[parameterTypes.length];
      for (int j = 0; j < arguments.length; j++) {
        Class type = parameterTypes[j];
        if (type == byte[].class) {
          arguments[j] = UUID.randomUUID().toString().getBytes();
        } else if (type == String.class) {
          arguments[j] = UUID.randomUUID().toString();
        } else {
          arguments[j] = mock(type);
        }
      }
      targetMethod.invoke(enclaveServer, arguments);
      targetMethod.invoke(verify(enclave), arguments);
    }
    verifyNoMoreInteractions(enclave);
  }
}
