package com.quorum.tessera.transaction;

import com.quorum.tessera.exception.OperationCurrentlySuspended;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TransactionManagerWrapperTest {

    private TransactionManagerWrapper transactionManagerWrapper;

    private TransactionManager transactionManager;

    private SyncState syncState;

    @Before
    public void onSetup() {

        transactionManager = mock(TransactionManager.class);

        syncState = mock(SyncState.class);

        transactionManagerWrapper = new TransactionManagerWrapper(transactionManager, syncState);

    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionManager, syncState);

    }

    @Test
    public void notResendJustDelegatedsToTransactionManager() throws Exception {
        when(syncState.isResendMode()).thenReturn(false);

        List<Method> methods = Arrays.asList(TransactionManager.class.getDeclaredMethods());

        for (Method m : methods) {

            Class<?>[] parameterTypes = m.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];

            m.invoke(transactionManagerWrapper, args);
            m.invoke(verify(transactionManager), args);

        }

        verify(syncState, times(methods.size())).isResendMode();
    }

    @Test
    public void storePayloadBypassResendMode() {

        byte[] data = "Hellow".getBytes();
        transactionManagerWrapper.storePayloadBypassResendMode(data);

        verify(transactionManager).storePayload(data);
        verifyZeroInteractions(syncState);
    }

    @Test
    public void enableResendJustDelegatedsToTransactionManager() throws Exception {
        when(syncState.isResendMode()).thenReturn(true);

        List<Method> methods = Arrays.asList(TransactionManager.class.getDeclaredMethods());

        for (Method m : methods) {

            Class<?>[] parameterTypes = m.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];

            try {
                m.invoke(transactionManagerWrapper, args);
                failBecauseExceptionWasNotThrown(InvocationTargetException.class);
            } catch (InvocationTargetException ex) {
                assertThat(ex).hasCauseExactlyInstanceOf(OperationCurrentlySuspended.class);
            }

        }

        verify(syncState, times(methods.size())).isResendMode();
    }

}
