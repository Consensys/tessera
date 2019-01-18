
package com.quorum.tessera.enclave;

import static org.mockito.Mockito.mock;

public class MockEnclaveFactory implements EnclaveFactory<Object> {
     public Enclave create(Object config) {
         return mock(Enclave.class);
     }
}
