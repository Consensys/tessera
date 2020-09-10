package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class FindRecipientFromPartyInfoTest {

    private FindRecipientFromPartyInfo findRecipientFromPartyInfo;

    private PartyInfoService partyInfoService;
    @Before
    public void onSetUp() {
        partyInfoService = mock(PartyInfoService.class);
        findRecipientFromPartyInfo = new FindRecipientFromPartyInfo(partyInfoService);

    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService);
    }

    @Test
    public void executeKeyFound() {

        BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
        PublicKey publicKey = mock(PublicKey.class);
        batchWorkflowContext.setRecipientKey(publicKey);

        PartyInfo partyInfo = mock(PartyInfo.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(publicKey);

        when(partyInfo.getRecipients()).thenReturn(Set.of(recipient));

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        boolean result = findRecipientFromPartyInfo.execute(batchWorkflowContext);
        assertThat(result).isTrue();

        assertThat(batchWorkflowContext.getRecipient()).isSameAs(recipient);

        verify(partyInfoService).getPartyInfo();


    }

    @Test
    public void executeKeyNotFound() {

        BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
        PublicKey publicKey = mock(PublicKey.class);
        batchWorkflowContext.setRecipientKey(publicKey);

        PartyInfo partyInfo = mock(PartyInfo.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(mock(PublicKey.class));

        when(partyInfo.getRecipients()).thenReturn(Set.of(recipient));

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        try {
            findRecipientFromPartyInfo.execute(batchWorkflowContext);
            failBecauseExceptionWasNotThrown(KeyNotFoundException.class);
        } catch (KeyNotFoundException ex) {
            verify(partyInfoService).getPartyInfo();
            assertThat(batchWorkflowContext.getRecipient()).isNull();
            assertThat(batchWorkflowContext.getRecipientKey()).isSameAs(publicKey);
        }

    }

}
