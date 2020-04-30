package com.quorum.tessera.recover;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.sync.TransactionRequester;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


import static org.mockito.Mockito.*;

@Ignore
public class RecoveryTest extends RecoveryTestCase {

    private Recovery recovery;

    private TransactionRequester transactionRequester;

    private PartyInfoService partyInfoService;

    @Before
    public void onSetUp() {

        partyInfoService = new MockPartyInfoServiceFactory().partyInfoService();
        when(partyInfoService.getPartyInfo()).thenReturn(getPartyInfo());

        transactionRequester = mock(TransactionRequester.class);
        when(transactionRequester.requestAllTransactionsFromNode(anyString())).thenReturn(true);

        MockTransactionRequesterFactory.setTransactionRequester(transactionRequester);

        this.recovery = RecoveryFactory.newFactory().create(getConfig());
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionRequester);
        verifyNoMoreInteractions(partyInfoService);
        MockTransactionRequesterFactory.setTransactionRequester(null);
    }



    @Test
    public void recover() {
        recovery.recover();

        verify(transactionRequester,times(4)).requestAllTransactionsFromNode(anyString());

        verify(partyInfoService).getPartyInfo();


    }

}
