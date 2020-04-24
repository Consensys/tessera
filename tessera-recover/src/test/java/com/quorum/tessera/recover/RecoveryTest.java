package com.quorum.tessera.recover;

import com.quorum.tessera.sync.TransactionRequester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RecoveryTest extends RecoveryTestCase {

    private Recovery recovery;

    private TransactionRequester transactionRequester;

    @Before
    public void onSetUp() {

        transactionRequester = mock(TransactionRequester.class);
        when(transactionRequester.requestAllTransactionsFromNode(anyString())).thenReturn(true);

        MockTransactionRequesterFactory.setTransactionRequester(transactionRequester);

        this.recovery = RecoveryFactory.newFactory().create(getConfig());
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionRequester);
        MockTransactionRequesterFactory.setTransactionRequester(null);
    }



    @Test
    public void recover() {
        recovery.recover();

        List<String> requestedUrls = new ArrayList<>();
        doAnswer(invocation -> {
            requestedUrls.add(invocation.getArgument(0));
            return null;
        }).when(transactionRequester).requestAllTransactionsFromNode(anyString());

        assertThat(requestedUrls).hasSize(5);

    }

}
